package me.tooshort;

import me.tooshort.networking.PostItNetworking;
import me.tooshort.entity.PostItEntities;
import me.tooshort.item.PostItItems;


public class Registration {
	public static void register() {
		PostItNetworking.register();
		PostItItems     .register();
		PostItEntities  .register();
	}
}
