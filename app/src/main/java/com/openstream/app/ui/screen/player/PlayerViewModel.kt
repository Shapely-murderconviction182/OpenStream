package com.openstream.app.ui.screen.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val isPlaying       : Boolean = false,
    val isBuffering     : Boolean = false,
    val showControls    : Boolean = true,
    val currentPosition : Long    = 0L,
    val duration        : Long    = 0L,
    val playbackSpeed   : Float   = 1.0f,
    val errorMessage    : String? = null,
    val title           : String  = ""
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context).build().also { exo ->
        exo.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
                if (isPlaying) scheduleHideControls()
            }
            override fun onPlaybackStateChanged(state: Int) {
                _state.value = _state.value.copy(
                    isBuffering = state == Player.STATE_BUFFERING
                )
            }
            override fun onPlayerError(error: PlaybackException) {
                _state.value = _state.value.copy(errorMessage = error.message ?: "Playback error")
            }
        })
    }

    private var hideJob: Job? = null

    fun prepare(url: String, title: String) {
        _state.value = _state.value.copy(title = title, errorMessage = null)
        val item = MediaItem.fromUri(url)
        player.setMediaItem(item)
        player.prepare()
        player.playWhenReady = true
        startPositionUpdates()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
        showControls()
    }

    fun seekRelative(offsetMs: Long) {
        player.seekTo((player.currentPosition + offsetMs).coerceAtLeast(0L))
        showControls()
    }

    fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    fun showControls() {
        _state.value = _state.value.copy(showControls = true)
        scheduleHideControls()
    }

    fun toggleControls() {
        if (_state.value.showControls) {
            hideJob?.cancel()
            _state.value = _state.value.copy(showControls = false)
        } else {
            showControls()
        }
    }

    fun retry() {
        _state.value = _state.value.copy(errorMessage = null)
        player.prepare()
        player.play()
    }

    private fun scheduleHideControls() {
        hideJob?.cancel()
        hideJob = viewModelScope.launch {
            delay(3_000L)
            _state.value = _state.value.copy(showControls = false)
        }
    }

    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (true) {
                _state.value = _state.value.copy(
                    currentPosition = player.currentPosition,
                    duration        = player.duration.coerceAtLeast(0L)
                )
                delay(500L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
