package gollorum.signpost.forge.security;

import gollorum.signpost.forge.PlayerHandle;

import java.util.Optional;

public interface WithOwner {

	public interface OfSignpost extends WithOwner {
		Optional<PlayerHandle> getSignpostOwner();
	}

	public interface OfWaystone extends WithOwner {
		Optional<PlayerHandle> getWaystoneOwner();
	}

}
