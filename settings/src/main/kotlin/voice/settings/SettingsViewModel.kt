package voice.settings

import de.paulwoitaschek.flowpref.Pref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import voice.common.DARK_THEME_SETTABLE
import voice.common.pref.PrefKeys
import javax.inject.Inject
import javax.inject.Named

class SettingsViewModel
@Inject constructor(
  @Named(PrefKeys.DARK_THEME)
  private val useDarkTheme: Pref<Boolean>,
  @Named(PrefKeys.RESUME_ON_REPLUG)
  private val resumeOnReplugPref: Pref<Boolean>,
  @Named(PrefKeys.AUTO_REWIND_AMOUNT)
  private val autoRewindAmountPref: Pref<Int>,
  @Named(PrefKeys.SEEK_TIME)
  private val seekTimePref: Pref<Int>
) : SettingsListener {

  private val _viewEffects = MutableSharedFlow<SettingsViewEffect>(extraBufferCapacity = 1)
  val viewEffects: Flow<SettingsViewEffect> get() = _viewEffects

  private val dialog = MutableStateFlow<SettingsViewState.Dialog?>(null)

  fun viewState(): Flow<SettingsViewState> {
    return combine(
      useDarkTheme.flow,
      resumeOnReplugPref.flow,
      autoRewindAmountPref.flow,
      seekTimePref.flow,
      dialog
    ) { useDarkTheme, resumeOnReplug, autoRewindAmount, seekTime, dialog ->
      SettingsViewState(
        useDarkTheme = useDarkTheme,
        showDarkThemePref = DARK_THEME_SETTABLE,
        resumeOnReplug = resumeOnReplug,
        seekTimeInSeconds = seekTime,
        autoRewindInSeconds = autoRewindAmount,
        dialog = dialog
      )
    }
  }

  override fun close() {
    SettingsViewEffect.CloseScreen.emit()
  }

  override fun toggleResumeOnReplug() {
    resumeOnReplugPref.value = !resumeOnReplugPref.value
  }

  override fun toggleDarkTheme() {
    useDarkTheme.value = !useDarkTheme.value
  }

  override fun seekAmountChanged(seconds: Int) {
    seekTimePref.value = seconds
  }

  override fun onSeekAmountRowClicked() {
    dialog.tryEmit(SettingsViewState.Dialog.SeekTime)
  }

  override fun autoRewindAmountChanged(seconds: Int) {
    autoRewindAmountPref.value = seconds
  }

  override fun onAutoRewindRowClicked() {
    dialog.tryEmit(SettingsViewState.Dialog.AutoRewindAmount)
  }

  override fun onLikeClicked() {
    dialog.tryEmit(SettingsViewState.Dialog.Contribute)
  }

  override fun dismissDialog() {
    dialog.tryEmit(null)
  }

  override fun openSupport() {
    dismissDialog()
    SettingsViewEffect.ToSupport.emit()
  }

  override fun openTranslations() {
    dismissDialog()
    SettingsViewEffect.ToTranslations.emit()
  }

  private fun SettingsViewEffect.emit() {
    _viewEffects.tryEmit(this)
  }
}
