

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.plugin.Plugin;

/**
 * Abstract class for taint analysis handlers that require to run on-the-fly
 * with pointer analysis.
 */
abstract class OnFlyHandler extends Handler implements Plugin {

    protected OnFlyHandler(HandlerContext context) {
        super(context);
    }
}
