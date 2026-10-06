#!/usr/bin/env bash
# Fetch Linear issues for triage.
#
# Usage:
#   linear-fetch.sh issue DX-123          # Single issue by identifier
#   linear-fetch.sh issue DX-123 DX-456  # Multiple issues
#   linear-fetch.sh triage                  # Triage issues for DX team (default)
#   linear-fetch.sh triage --team OTHER    # Triage issues for a different team
#
# Requires LINEAR_API_KEY in the project .env file.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"
LINEAR_API="https://api.linear.app/graphql"
DEFAULT_TEAM="DX"

# ---------------------------------------------------------------------------
# load_env — loads LINEAR_API_KEY from .env
# ---------------------------------------------------------------------------
load_env() {
  local env_file="${PROJECT_ROOT}/.env"
  if [ -f "${env_file}" ]; then
    set -a
    # shellcheck source=/dev/null
    source "${env_file}"
    set +a
  fi
  : "${LINEAR_API_KEY:?Missing LINEAR_API_KEY in ${env_file}}"
}

# ---------------------------------------------------------------------------
# gql — execute a GraphQL query against the Linear API
# ---------------------------------------------------------------------------
gql() {
  local query="$1"
  local variables="${2:-"{}"}"

  local payload
  payload=$(node -e "process.stdout.write(JSON.stringify({query: process.argv[1], variables: JSON.parse(process.argv[2])}))" "$query" "$variables")

  # Fail here, rather than let callers select `.data` from an error and print nothing: an HTTP error,
  # a body that isn't JSON, a GraphQL `errors` array or a missing `data` all stop the script.
  local response status
  if ! response=$(curl -sS -X POST "${LINEAR_API}" \
    -H "Content-Type: application/json" \
    -H "Authorization: ${LINEAR_API_KEY}" \
    -d "$payload" -w '\n%{http_code}'); then
    echo "error: request to Linear failed" >&2
    return 1
  fi
  status="${response##*$'\n'}"
  response="${response%$'\n'*}"
  if ! jq -e 'type == "object"' <<< "$response" > /dev/null 2>&1; then
    echo "error: Linear returned HTTP ${status} with a body that isn't JSON" >&2
    return 1
  fi
  if [ "$status" -ge 400 ] || [ "$(jq 'has("errors") or (.data == null)' <<< "$response")" = "true" ]; then
    echo "error: Linear returned HTTP ${status}:" >&2
    jq -r '(.errors // [{ message: "the response has no data" }])[] | "  " + (.message // tostring)' <<< "$response" >&2
    return 1
  fi
  printf '%s\n' "$response"
}

# ---------------------------------------------------------------------------
# fetch_issue — fetch a single issue by identifier (e.g. DX-123)
# ---------------------------------------------------------------------------
fetch_issue() {
  local identifier="$1"

  local query='
    query($filter: IssueFilter) {
      issues(filter: $filter, first: 1) {
        nodes {
          identifier
          title
          description
          priority
          priorityLabel
          state { name type }
          assignee { name }
          creator { name }
          labels { nodes { name } }
          comments { nodes { body user { name } createdAt } }
          createdAt
          updatedAt
          url
          parent { identifier title }
          children { nodes { identifier title state { name } } }
          relations { nodes { type relatedIssue { identifier title state { name } } } }
        }
      }
    }
  '

  # Extract team key and number from identifier (e.g. DX-123)
  local team_key="${identifier%%-*}"
  local number="${identifier##*-}"

  local variables
  variables=$(jq -n \
    --arg team "$team_key" \
    --argjson num "$number" \
    '{ filter: { team: { key: { eq: $team } }, number: { eq: $num } } }')

  gql "$query" "$variables"
}

# ---------------------------------------------------------------------------
# fetch_triage — fetch issues in Triage state
# ---------------------------------------------------------------------------
fetch_triage() {
  local team_key="${DEFAULT_TEAM}"

  while [[ $# -gt 0 ]]; do
    case "$1" in
      --team) team_key="$2"; shift 2 ;;
      *) shift ;;
    esac
  done

  local query='
    query($filter: IssueFilter, $after: String) {
      issues(filter: $filter, first: 50, after: $after, orderBy: createdAt) {
        pageInfo { hasNextPage endCursor }
        nodes {
          identifier
          title
          description
          priority
          priorityLabel
          state { name type }
          assignee { name }
          creator { name }
          labels { nodes { name } }
          createdAt
          updatedAt
          url
          parent { identifier title }
          relations { nodes { type relatedIssue { identifier title state { name } } } }
        }
      }
    }
  '

  local filter
  if [ -n "$team_key" ]; then
    filter=$(jq -n --arg team "$team_key" \
      '{ state: { type: { eq: "triage" } }, team: { key: { eq: $team } } }')
  else
    filter=$(jq -n '{ state: { type: { eq: "triage" } } }')
  fi

  # Linear returns at most 50 issues per request, so follow the cursor until the last page and
  # print every page's issues as one JSON array.
  local after="null" all="[]" page
  while :; do
    page=$(gql "$query" "$(jq -n --argjson filter "$filter" --argjson after "$after" \
      '{ filter: $filter, after: $after }')") || return 1
    all=$(jq -s '.[0] + .[1].data.issues.nodes' <(echo "$all") <(echo "$page"))
    [ "$(jq '.data.issues.pageInfo.hasNextPage' <<< "$page")" = "true" ] || break
    after=$(jq '.data.issues.pageInfo.endCursor' <<< "$page")
  done
  echo "$all"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
load_env

command="${1:-}"
shift || true

case "$command" in
  issue)
    if [ $# -eq 0 ]; then
      echo "Usage: linear-fetch.sh issue DX-123 [DX-456 ...]" >&2
      exit 1
    fi
    # Fetch each issue and collect results
    # Fetch every identifier, then fail if any weren't found, so callers can't mistake a partial
    # result for a complete one but still get the issues that do exist.
    missing=()
    for id in "$@"; do
      echo "--- ${id} ---"
      response=$(fetch_issue "$id") || exit 1
      if ! jq -e '.data.issues.nodes[0] // empty' <<< "$response"; then
        echo "error: ${id} not found" >&2
        missing+=("$id")
      fi
    done
    if [ ${#missing[@]} -gt 0 ]; then
      echo "error: not found: ${missing[*]}" >&2
      exit 1
    fi
    ;;
  triage)
    fetch_triage "$@" || exit 1
    ;;
  *)
    echo "Usage: linear-fetch.sh <issue|triage> [args...]" >&2
    echo ""
    echo "Commands:"
    echo "  issue DX-123 [DX-456 ...]   Fetch specific issues by identifier"
    echo "  triage [--team DX]            Fetch all issues in Triage state"
    exit 1
    ;;
esac
