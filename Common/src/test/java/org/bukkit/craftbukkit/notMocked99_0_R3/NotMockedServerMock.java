package org.bukkit.craftbukkit.notMocked99_0_R3;

import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils.IMockedServer;
import org.bukkit.craftbukkit.mocked0_0_R1.VersionedServerMock;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The class used to test whether {@link AdvancementMain#load()} actually throws an {@link InvalidVersionException} when
 * it is loaded on a not supported server version.
 * <p>Using this class allows {@link ReflectionUtil} to not throw an exception when creating the error message of {@link InvalidVersionException}.
 * <p>This differs from {@link VersionedServerMock} since there <strong>isn't</strong> a mocked implementation of the
 * nms wrappers in the package {@code com.fren_gor.ultimateAdvancementAPI.nms.notMocked99_0_R3} (even the package doesn't exist).
 */
public class NotMockedServerMock extends ServerMock implements IMockedServer {
    /**
     * Creates a new {@code NotMockedServerMock}.
     *
     * @throws IllegalStateException If the {@code com.fren_gor.ultimateAdvancementAPI.nms.notMocked99_0_R3} package exists.
     */
    public NotMockedServerMock() {
        if (getClass().getClassLoader().getDefinedPackage("com.fren_gor.ultimateAdvancementAPI.nms.notMocked99_0_R3") != null) {
            throw new IllegalStateException("The package com.fren_gor.ultimateAdvancementAPI.nms.notMocked99_0_R3 exists but it shouldn't.");
        }
    }

    @NotNull
    @Override
    public Optional<String> getMockedVersion() {
        return Optional.empty();
    }
}
