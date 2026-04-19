package com.aven.chessbot.config;

import com.aven.chessbot.protocol.UCI;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "chessbot.uci.enabled", havingValue = "true")
public class UciRunner implements CommandLineRunner {
  private final UCI uci;

  public UciRunner(UCI uci) {
    this.uci = uci;
  }

  @Override
  public void run(String... args) throws Exception {
    uci.uciCommunication();
  }
}
