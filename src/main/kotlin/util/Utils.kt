package me.geek.tom.mcupdateinfo.util

import java.io.File
import java.io.IOException
import java.net.URI

import java.net.URISyntaxException




fun File.toJarURI(): URI {
    val uri = toURI()
    val jarUri: URI
    try {
        jarUri = URI("jar:" + uri.scheme, uri.host, uri.path, uri.fragment)
    } catch (e: URISyntaxException) {
        throw IOException(e)
    }
    return jarUri
}
