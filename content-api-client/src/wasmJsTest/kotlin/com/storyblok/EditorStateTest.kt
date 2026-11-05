@file:OptIn(ExperimentalWasmJsInterop::class, InternalAPI::class)

package com.storyblok

import com.storyblok.cdn.BlockAction
import com.storyblok.cdn.BlockBreadcrumb
import com.storyblok.cdn.BlockEdit
import com.storyblok.cdn.BlockHints
import com.storyblok.cdn.BlockMenu
import com.storyblok.cdn.EditableBlock
import com.storyblok.cdn.EditorState
import com.storyblok.cdn.SelectedBlock
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * What the Visual Editor's messages other than `input` do to the preview — the selected block's menu
 * and the hints — and the messages the menu sends back, all shaped as `@storyblok/preview-bridge`
 * reads and writes them.
 */
class EditorStateTest {

    private val state = EditorState()

    private fun receive(json: String) = state.receive(parse(json))

    /** The selected block, with its menu as the author sees it: without the breadcrumb actions are sent with. */
    private val selected get() = state.selectedBlock.value?.let { it.copy(menu = it.menu?.copy(target = null)) }

    private val enterEditmode = """{"action": "enterEditmode", "storyId": "42", "appVersion": "v2",
        "componentNames": {"page": "Page", "grid": "Grid", "teaser": "Teaser Card"}}"""

    /** A teaser in a grid on the page, the grid's third block, which the editor lets the author do anything to. */
    private val editedTeaser = """{"action": "editedBlok", "blok": {"uid": "u3", "name": "teaser"},
        "breadcrumbs": [{"_uid": "page", "component": "page"}, {"_uid": "u2", "component": "grid", "_parentindex": 0},
                        {"_uid": "u3", "component": "teaser", "_parentindex": 2, "_parent": "u2"}],
        "canAddBlocks": true, "canMoveForward": true, "canMoveBackward": true, "canDeleteBlocks": true}"""

    private val breadcrumbs = listOf(
        BlockBreadcrumb("page", "page", "Page"),
        BlockBreadcrumb("u2", "grid", "Grid"),
        BlockBreadcrumb("u3", "teaser", "Teaser Card"),
    )

    @Test
    fun `the block the editor opens gets a menu with every action it allows`() {
        receive(enterEditmode)
        receive(editedTeaser)

        assertEquals(
            SelectedBlock(
                "u3",
                ancestors = listOf("page", "u2"),
                reveal = 1,
                menu = BlockMenu("Teaser Card", breadcrumbs, BlockAction.entries),
            ),
            selected,
        )
    }

    @Test
    fun `a block clicked in the form gets a menu`() {
        receive(enterEditmode)
        receive(editedTeaser.replace(""""action": "editedBlok"""", """"action": "enterComponent", "storyId": "42", "componentId": "u3""""))

        assertEquals(BlockMenu("Teaser Card", breadcrumbs, BlockAction.entries), selected?.menu)
    }

    @Test
    fun `only the editor's second version lets a block be copied`() {
        receive(editedTeaser)

        assertEquals(BlockAction.entries - BlockAction.Copy, state.selectedBlock.value?.menu?.actions)
    }

    @Test
    fun `each permission adds its actions`() {
        receive(enterEditmode)
        fun actions(permission: String): List<BlockAction>? {
            receive(
                """{"action": "editedBlok", "blok": {"uid": "u3", "name": "teaser"}, "$permission": true,
                    "breadcrumbs": [{"_uid": "page", "component": "page"}, {"_uid": "u3", "component": "teaser", "_parentindex": 1}]}"""
            )
            return state.selectedBlock.value?.menu?.actions
        }

        assertEquals(listOf(BlockAction.AddBlockBefore, BlockAction.AddBlockAfter, BlockAction.Duplicate, BlockAction.Copy), actions("canAddBlocks"))
        assertEquals(listOf(BlockAction.Copy, BlockAction.MoveForward), actions("canMoveForward"))
        assertEquals(listOf(BlockAction.Copy, BlockAction.MoveBackward), actions("canMoveBackward"))
        assertEquals(listOf(BlockAction.Copy, BlockAction.Delete), actions("canDeleteBlocks"))
    }

    @Test
    fun `the first block in its field cannot move backward`() {
        receive(editedTeaser.replace(""""_parentindex": 2""", """"_parentindex": 0"""))

        assertEquals(false, BlockAction.MoveBackward in state.selectedBlock.value?.menu?.actions.orEmpty())
    }

    @Test
    fun `a block whose index the editor does not give cannot move backward`() {
        receive(editedTeaser.replace(""""_parentindex": 2,""", ""))

        assertEquals(false, BlockAction.MoveBackward in state.selectedBlock.value?.menu?.actions.orEmpty())
    }

    @Test
    fun `the story's content is labelled with no actions`() {
        receive(enterEditmode)
        receive(
            """{"action": "editedBlok", "blok": {"uid": "page", "name": "page"}, "breadcrumbs": [{"_uid": "page", "component": "page"}],
                "canAddBlocks": true, "canDeleteBlocks": true}"""
        )

        assertEquals(BlockMenu("Page", listOf(BlockBreadcrumb("page", "page", "Page")), emptyList()), selected?.menu)
    }

    @Test
    fun `a block with no display name is labelled with its name made readable`() {
        receive(
            """{"action": "editedBlok", "blok": {"uid": "u3", "name": "hero_section"},
                "breadcrumbs": [{"_uid": "page", "component": "landingPage"}, {"_uid": "u3", "component": "hero_section"}]}"""
        )

        val menu = state.selectedBlock.value?.menu
        assertEquals("Hero Section", menu?.label)
        assertEquals(listOf("Landing Page", "Hero Section"), menu?.breadcrumbs?.map { it.label })
    }

    @Test
    fun `other messages leave the menu as it was`() {
        receive(editedTeaser)
        val selected = state.selectedBlock.value

        receive("""{"action": "input", "story": {"id": 42, "content": {}}}""")
        receive("""{"action": "hoverComponent", "storyId": "42", "componentId": "u2"}""")
        receive("""{"action": "highlightComponent", "storyId": "42", "componentIds": ["u2"], "componentId": "u2"}""")

        assertEquals(selected, state.selectedBlock.value)
    }

    @Test
    fun `a block added moved or duplicated has no menu until the editor describes it`() {
        receive(editedTeaser)

        receive("""{"action": "movedBlock", "blockId": "u3"}""")

        assertEquals(SelectedBlock("u3", ancestors = listOf("page", "u2"), reveal = 2), state.selectedBlock.value)
    }

    @Test
    fun `a new menu for the selected block leaves it where it is`() {
        receive(editedTeaser)

        receive(editedTeaser.replace(""""canDeleteBlocks": true""", """"canDeleteBlocks": false"""))

        assertEquals(1, state.selectedBlock.value?.reveal)
        assertEquals(false, BlockAction.Delete in state.selectedBlock.value?.menu?.actions.orEmpty())
    }

    @Test
    fun `tapping the selected block keeps its menu`() {
        receive(editedTeaser)
        val selected = state.selectedBlock.value

        state.open(EditableBlock(name = "teaser", uid = "u3", id = "42"))

        assertEquals(selected, state.selectedBlock.value)
    }

    @Test
    fun `tapping another block selects it without a menu`() {
        receive(editedTeaser)

        state.open(EditableBlock(name = "teaser", uid = "u4", id = "42"))

        // Tapped, so on screen already: no new request to bring it into view.
        assertEquals(SelectedBlock("u4", reveal = 1), state.selectedBlock.value)
    }

    @Test
    fun `an action is sent with the block's breadcrumb as the editor sent it`() {
        receive(editedTeaser)

        val message = state.action(BlockAction.Delete)!!

        assertEquals("deleteBlock", message.action)
        assertEquals(
            """{"_uid":"u3","component":"teaser","_parentindex":2,"_parent":"u2"}""",
            stringify(message.blok),
        )
    }

    @Test
    fun `every action is sent under the editor's name for it`() {
        receive(enterEditmode)
        receive(editedTeaser)

        assertEquals(
            listOf("addBlockBefore", "addBlockAfter", "duplicateBlock", "copy", "moveForward", "moveBackward", "deleteBlock"),
            BlockAction.entries.map { state.action(it)?.action },
        )
    }

    @Test
    fun `an action the menu does not offer is not sent`() {
        receive(editedTeaser.replace(""""canDeleteBlocks": true""", """"canDeleteBlocks": false"""))

        assertNull(state.action(BlockAction.Delete))
    }

    @Test
    fun `a parent chosen from the menu has no actions until the editor describes it`() {
        receive(enterEditmode)
        receive(editedTeaser)

        state.open(breadcrumbs[1], "", null)

        // The teaser's breadcrumb would name the wrong block.
        assertNull(state.action(BlockAction.Delete))
    }

    @Test
    fun `no action is sent for a block without a menu`() {
        receive("""{"action": "addedBlock", "blockId": "u9"}""")

        assertNull(state.action(BlockAction.Delete))
    }

    @Test
    fun `a breadcrumb opens its block's form in the story being edited`() {
        receive(enterEditmode)
        receive(editedTeaser)

        val message = state.open(breadcrumbs[1], "feed.posts", null)!!

        assertEquals("edit", message.action)
        assertEquals(listOf("grid", "u2", "42"), listOf(message.dataC.name, message.dataC.uid, message.dataC.id))
        assertEquals("""["feed.posts"]""", stringify(message.config.resolveRelations))
        // Nesting the selected block, so on screen already.
        assertEquals(SelectedBlock("u2", ancestors = listOf("page"), reveal = 1), state.selectedBlock.value)
    }

    @Test
    fun `the story being edited is known from its updates`() {
        receive("""{"action": "input", "story": {"id": 42, "content": {}}}""")
        receive(editedTeaser)

        assertEquals("42", state.open(breadcrumbs[1], "", null)?.dataC?.id)
    }

    @Test
    fun `the story being edited is known from a tapped block`() {
        state.open(EditableBlock(name = "teaser", uid = "u3", id = "7"))
        receive(editedTeaser)

        assertEquals("7", state.open(breadcrumbs[1], "", null)?.dataC?.id)
    }

    @Test
    fun `no breadcrumb is opened before the story being edited is known`() {
        receive(editedTeaser)

        assertNull(state.open(breadcrumbs[1], "", null))
        assertEquals("u3", state.selectedBlock.value?.uid)
    }

    @Test
    fun `hovering over a block in the form hints at it`() {
        receive("""{"action": "hoverComponent", "storyId": "42", "componentId": "u2"}""")

        assertEquals(BlockHints(hovered = "u2"), state.hints.value)
    }

    @Test
    fun `the hint goes once dismissed`() {
        receive("""{"action": "hoverComponent", "storyId": "42", "componentId": "u2"}""")

        state.dismissHover()

        assertEquals(BlockHints(), state.hints.value)
    }

    @Test
    fun `the hint goes once a block is clicked in the form`() {
        receive("""{"action": "hoverComponent", "storyId": "42", "componentId": "u2"}""")

        receive("""{"action": "enterComponent", "storyId": "42", "componentId": "u3"}""")

        assertNull(state.hints.value.hovered)
    }

    @Test
    fun `the editor highlights blocks and names one to bring into view`() {
        receive("""{"action": "highlightComponent", "storyId": "42", "componentIds": ["u2", "u3"], "componentId": "u3"}""")

        assertEquals(BlockHints(highlighted = listOf("u2", "u3"), highlightedFocus = "u3"), state.hints.value)
    }

    @Test
    fun `highlighting nothing clears the highlights`() {
        receive("""{"action": "highlightComponent", "storyId": "42", "componentIds": ["u2"], "componentId": "u2"}""")

        receive("""{"action": "highlightComponent", "storyId": "42", "componentIds": []}""")

        assertEquals(BlockHints(), state.hints.value)
    }

    private fun input(id: Int, headline: String) =
        """{"action": "input", "story": {"id": $id, "content": {"_uid": "page", "component": "page",
            "body": [{"_uid": "u3", "component": "teaser", "headline": "$headline"}]}}}"""

    @Test
    fun `the first update of a story is put in the selected block`() {
        receive(editedTeaser)
        receive(input(42, "Hi"))

        assertEquals(BlockEdit("u3", ancestors = listOf("page", "u2"), compared = false, serial = 1), state.hints.value.edited)
    }

    @Test
    fun `the first update of a story with nothing selected is put in its content`() {
        receive(input(42, "Hi"))

        assertEquals(BlockEdit("page", compared = false, serial = 1), state.hints.value.edited)
    }

    @Test
    fun `an update names the block it changed`() {
        receive(input(42, "Hi"))
        receive(input(42, "Hello"))

        assertEquals(BlockEdit("u3", ancestors = listOf("page"), serial = 2), state.hints.value.edited)
    }

    @Test
    fun `each edit is told apart from the one before`() {
        receive(input(42, "Hi"))
        receive(input(42, "Hello"))
        receive(input(42, "Hello!"))

        assertEquals(3, state.hints.value.edited?.serial)
    }

    @Test
    fun `an update that changes nothing leaves the last edit`() {
        receive(input(42, "Hi"))
        receive(input(42, "Hello"))
        receive(input(42, "Hello"))

        assertEquals(2, state.hints.value.edited?.serial)
    }

    @Test
    fun `another story's update is not compared with the last`() {
        receive(input(42, "Hi"))
        receive(input(43, "Hello"))

        assertEquals(false, state.hints.value.edited?.compared)
    }

    @Test
    fun `a message that is not an object is ignored`() {
        state.receive(parse("\"editedBlok\""))
        state.receive(null)

        assertNull(state.selectedBlock.value)
        assertEquals(BlockHints(), state.hints.value)
    }
}

private fun parse(text: String): JsAny = js("JSON.parse(text)")

private fun stringify(value: JsAny?): String = js("JSON.stringify(value)")
