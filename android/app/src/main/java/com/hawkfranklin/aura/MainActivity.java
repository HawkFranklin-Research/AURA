package com.hawkfranklin.aura;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(GenAIPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
