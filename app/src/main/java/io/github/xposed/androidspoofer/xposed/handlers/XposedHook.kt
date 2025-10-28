package io.github.xposed.androidspoofer.xposed.handlers

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

/**
 * Interface for all Xposed hook handlers
 */
interface XposedHook {
    /**
     * Tag used for logging, automatically derived from the class name
     */
    val TAG: String
        get() = this.javaClass.simpleName

    /**
     * Apply hooks for the given package with the specified replacement value
     *
     * @param lpparam LoadPackageParam containing package information
     * @param newValue The new value to replace with (format depends on implementation)
     */
    fun hook(lpparam: LoadPackageParam, newValue: String)
}

