package com.storyblok.ktor

import io.ktor.http.Url

/**
 * The API server location, the base URL for the API depends on the server location of the space.
 *
 * Learn more in the [Content Delivery API Reference](https://www.storyblok.com/docs/api/content-delivery/v2)
 * or [Management API Reference](https://www.storyblok.com/docs/api/management).
 */
public sealed class Region(internal val cdnUrl: Url, internal val mapiUrl: Url) {
    private constructor(cdn: String, mapi: String) : this(Url(cdn), Url(mapi))
    /** European Union API server location. */
    public object EU: Region("https://api.storyblok.com/v2/cdn/", "https://mapi.storyblok.com/v1/")
    /** United States API server location. */
    public object USA: Region("https://api-us.storyblok.com/v2/cdn/","https://api-us.storyblok.com/")
    /** Canada API server location. */
    public object CAN: Region("https://api-ca.storyblok.com/v2/cdn/", "https://api-ca.storyblok.com/")
    /** Australia API server location. */
    public object AUS: Region("https://api-ap.storyblok.com/v2/cdn/","https://api-ap.storyblok.com/")
    /** China API server location. */
    public object CHN: Region("https://app.storyblokchina.cn/", "https://app.storyblokchina.cn/")
    /** Custom API server location
     * @param url the custom base URL
     * */
    public class Custom(url: String): Region(url, url)
}