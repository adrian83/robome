package com.github.adrian83.robome.domain.table;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.datastax.driver.core.Session;

// TODO How to test this?

@ExtendWith(MockitoExtension.class)
public class TableRepositoryTest {

  @Mock private Session sessionMock;
  @InjectMocks private TableRepository tableRespository;

  @Test
  public void canary() {
    assertTrue(true);
  }
}
