package com.blockymarketplace.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class MarketplaceEventData {
    public String action;
    public String target;

    private static final KeyedCodec<String> ACTION_KEY = new KeyedCodec<>("Action", Codec.STRING);
    private static final KeyedCodec<String> TARGET_KEY = new KeyedCodec<>("Target", Codec.STRING);

    public static final BuilderCodec<MarketplaceEventData> CODEC =
        BuilderCodec.builder(MarketplaceEventData.class, MarketplaceEventData::new)
            .append(ACTION_KEY,
                (obj, val) -> obj.action = val,
                obj -> obj.action)
            .add()
            .append(TARGET_KEY,
                (obj, val) -> obj.target = val,
                obj -> obj.target)
            .add()
            .build();
}
