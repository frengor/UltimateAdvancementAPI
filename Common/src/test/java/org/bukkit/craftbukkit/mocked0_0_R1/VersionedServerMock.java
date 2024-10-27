package org.bukkit.craftbukkit.mocked0_0_R1;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils.AbstractMockedServer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The class used to mock the server (see {@link MockBukkit#mock(ServerMock)}).
 * <p>Using this class allows {@link ReflectionUtil} to work correctly. A mocked implementation of nms wrappers
 * can be placed in the package {@code com.fren_gor.ultimateAdvancementAPI.nms.mocked0_0_R1}.
 */
public class VersionedServerMock extends AbstractMockedServer {
    @NotNull
    @Override
    public Optional<String> getMockedVersion() {
        return Optional.of("mocked0_0_R1");
    }
}
