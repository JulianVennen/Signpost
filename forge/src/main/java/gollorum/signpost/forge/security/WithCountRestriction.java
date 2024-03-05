package gollorum.signpost.forge.security;

import gollorum.signpost.forge.BlockRestrictions;

public interface WithCountRestriction {

	BlockRestrictions.Type getBlockRestrictionType();

}
