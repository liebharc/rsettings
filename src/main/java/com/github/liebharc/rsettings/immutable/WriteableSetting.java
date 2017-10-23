package com.github.liebharc.rsettings.immutable;

/**
 * A marker interface which indicates whether a settings value can be changed by an user. 
 * 
 * A read-only setting (a setting without this interface) may still change its own value 
 * during the update phase. Therefore a read-only setting is not a constant value.
 * 
 * Note: Marker interfaces are generally to be avoided. In this particular case it seems
 * to be okay, because settings (@see ReadSetting) and state (@see SettingState) have been seperated
 * that means that there is no method on settings which allow to change state (e.g. no setValue method).
 * However it is still a property of the setting if it can be changed or not and therefore the setting
 * type should indicate whether or not user changes are allowed.   
 */
public interface WriteableSetting {

}
