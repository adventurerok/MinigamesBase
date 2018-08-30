package com.ithinkrok.minigames.base.generation;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class VoidGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        for(int cz = 0; cz < 16; ++cz) {
            for(int cx = 0; cx < 16; ++cx) {
                biome.setBiome(cx, cz, Biome.JUNGLE);
            }
        }

        return createChunkData(world);
    }
}
