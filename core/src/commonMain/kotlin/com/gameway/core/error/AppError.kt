package com.gameway.core.error

sealed interface AppError {
    data class StorageError(val message: String) : AppError
    data class SerializationError(val message: String) : AppError
    data object LevelNotFoundError : AppError
    data object ChapterNotFoundError : AppError
    data class ValidationError(val field: String, val message: String) : AppError
    data class UnknownError(val throwable: Throwable? = null) : AppError
    
    fun toUserMessage(): String = when (this) {
        is StorageError -> "存储错误：$message"
        is SerializationError -> "数据解析错误：$message"
        LevelNotFoundError -> "关卡未找到"
        ChapterNotFoundError -> "章节未找到"
        is ValidationError -> "验证错误：$field - $message"
        is UnknownError -> "未知错误"
    }
}