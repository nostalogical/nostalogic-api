package net.nostalogic.datamodel

class NoPageResponse<T>(val page: Int, val size: Int, val hasNext: Boolean?, val content: List<T>)
