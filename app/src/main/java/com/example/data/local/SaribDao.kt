package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SaribDao {

    // Channels
    @Query("SELECT * FROM channels WHERE isEnabled = 1 ORDER BY sortOrder ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isEnabled = 1 ORDER BY sortOrder ASC")
    suspend fun getAllChannelsList(): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE categoryId = :categoryId AND isEnabled = 1 ORDER BY sortOrder ASC")
    fun getChannelsByCategory(categoryId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE categoryId = :categoryId AND isEnabled = 1 ORDER BY sortOrder ASC")
    suspend fun getChannelsListByCategory(categoryId: String): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE isEnabled = 1 ORDER BY viewsCount DESC LIMIT 10")
    fun getMostWatchedChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' AND isEnabled = 1")
    fun searchChannels(query: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels WHERE id = :id")
    suspend fun deleteChannelById(id: String)

    @Query("DELETE FROM channels")
    suspend fun clearAllChannels()

    // Categories
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE categoryType = :type")
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE categoryType IN (:types)")
    fun getCategoriesByTypes(types: List<String>): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()

    // Matches
    @Query("SELECT * FROM matches ORDER BY matchDate ASC, matchTime ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY matchDate ASC, matchTime ASC")
    suspend fun getAllMatchesList(): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE matchDate = :date ORDER BY matchTime ASC")
    fun getMatchesByDate(date: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE isLive = 1")
    fun getLiveMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Delete
    suspend fun deleteMatch(match: MatchEntity)

    @Query("DELETE FROM matches")
    suspend fun clearAllMatches()

    // Media (Movies, Series, Anime)
    @Query("SELECT * FROM media_items WHERE type = :type")
    fun getMediaByType(type: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE type = :type")
    suspend fun getMediaListByType(type: String): List<MediaEntity>

    @Query("SELECT * FROM media_items WHERE isTop = 1")
    fun getTopMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%'")
    fun searchMedia(query: String): Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(items: List<MediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(item: MediaEntity)

    @Delete
    suspend fun deleteMediaItem(item: MediaEntity)

    @Query("DELETE FROM media_items")
    suspend fun clearAllMedia()

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemId = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE itemId = :id")
    suspend fun removeFavorite(id: String)

    // API Sources
    @Query("SELECT * FROM api_sources")
    fun getAllApiSources(): Flow<List<ApiSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiSource(source: ApiSourceEntity)

    @Update
    suspend fun updateApiSource(source: ApiSourceEntity)

    @Delete
    suspend fun deleteApiSource(source: ApiSourceEntity)

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 50")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE id = :id LIMIT 1")
    suspend fun getWatchHistoryById(id: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(item: WatchHistoryEntity)

    @Query("UPDATE watch_history SET progressMs = :progressMs, durationMs = :durationMs, watchedAt = :watchedAt WHERE id = :id")
    suspend fun updateWatchProgress(id: String, progressMs: Long, durationMs: Long, watchedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteWatchHistoryById(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()

    // Counts for Admin Dashboard
    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getChannelsCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE type = 'MOVIE'")
    suspend fun getMoviesCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE type = 'SERIES'")
    suspend fun getSeriesCount(): Int

    @Query("SELECT COUNT(*) FROM matches")
    suspend fun getMatchesCount(): Int

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'DOWNLOADING' OR status = 'PAUSED'")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'COMPLETED' ORDER BY completedAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET progress = :progress, bytesDownloaded = :bytesDownloaded, totalBytes = :totalBytes, speedBps = :speedBps, etaSeconds = :etaSeconds, status = :status WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, progress: Int, bytesDownloaded: Long, totalBytes: Long, speedBps: Long, etaSeconds: Long, status: String)

    @Query("UPDATE downloads SET status = :status, completedAt = :completedAt, localFilePath = :filePath WHERE id = :id")
    suspend fun markDownloadCompleted(id: String, filePath: String, status: String = "COMPLETED", completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE downloads SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun markDownloadFailed(id: String, errorMessage: String, status: String = "FAILED")

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: String)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()
}
