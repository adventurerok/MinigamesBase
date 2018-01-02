package com.ithinkrok.minigames.base.bukkitlistener;

import com.ithinkrok.minigames.api.Game;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.api.user.User;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created by paul on 27/02/16.
 */
@RunWith(DataProviderRunner.class)
public class GameBukkitListenerTest {

    @Mock
    public Game game;

    public String gameGroupName = "test1_test_0";

    @Mock
    public GameGroup gameGroup;

    public String worldName = "test-0001";

    @Mock
    public World world;

    public GameBukkitListener sut;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);


        doReturn(gameGroupName).when(gameGroup).getName();
        doReturn(gameGroup).when(game).getGameGroup(gameGroupName);

        doReturn(worldName).when(world).getName();
        doReturn(gameGroup).when(game).getGameGroupFromWorldName(worldName);

        doReturn(Collections.emptyList()).when(world).getEntities();

        sut = new GameBukkitListener(game);
    }

    @Test
    @DataProvider({"20,10", "3,1", "4,0"})
    public void eventEntityDamagedShouldNotKillWithPositiveHealth(double health, double finalDamage) {
        EntityDamageEvent event = mock(EntityDamageEvent.class);

        doReturn(finalDamage).when(event).getFinalDamage();
        doReturn(finalDamage).when(event).getDamage();

        User user = createMockUser();
        LivingEntity entity = user.getEntity();

        doReturn(health).when(entity).getHealth();
        doReturn(health).when(user).getHealth();

        doReturn(entity).when(event).getEntity();

        EntityDamageEvent.DamageCause anyCause = EntityDamageEvent.DamageCause.CUSTOM;

        doReturn(anyCause).when(event).getCause();


        doAnswer(invocation -> {
         Object userEvent = invocation.getArguments()[0];

            if(userEvent instanceof UserDeathEvent){
                fail("No UserDeathEvent should be called");
            }

            return null;
        }).when(gameGroup).userEvent(any());

        sut.eventEntityDamaged(event);
    }

    private <T extends Entity> T createMockEntity(Class<T> clazz) {
        T result = mock(clazz);

        doReturn(world).when(result).getWorld();

        doReturn(new Location(world, 0, 0, 0)).when(result).getLocation();

        return result;
    }

    private User createMockUser() {
        User result = mock(User.class);

        doReturn(gameGroup).when(result).getGameGroup();

        Player player = createMockEntity(Player.class);

        UUID uuid = UUID.randomUUID();

        doReturn(uuid).when(player).getUniqueId();
        doReturn(uuid).when(result).getUuid();
        doReturn(result).when(gameGroup).getUser(uuid);

        doReturn(player).when(result).getEntity();
        doReturn(player).when(result).getPlayer();

        doReturn(player.getLocation()).when(result).getLocation();

        return result;
    }


}