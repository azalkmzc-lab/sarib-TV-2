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

    // Counts for Admin Dashboard
    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getChannelsCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE type = 'MOVIE'")
    suspend fun getMoviesCount(): Int

    @Query("SELECT COUNT(*) FROM media_items WHERE type = 'SERIES'")
    suspend fun getSeriesCount(): Int

    @Query("SELECT COUNT(*) FROM matches")
    suspend fun getMatchesCount(): Int
}
