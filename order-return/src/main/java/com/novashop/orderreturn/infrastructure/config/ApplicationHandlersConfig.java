package com.novashop.orderreturn.infrastructure.config;

import com.novashop.orderreturn.application.ApplyRefundTerminalHandler;
import com.novashop.orderreturn.application.ApproveResult;
import com.novashop.orderreturn.application.ApproveReturnCommand;
import com.novashop.orderreturn.application.ApproveReturnHandler;
import com.novashop.orderreturn.application.CreateReturnCommand;
import com.novashop.orderreturn.application.CreateReturnHandler;
import com.novashop.orderreturn.application.GetReturnHandler;
import com.novashop.orderreturn.application.ListPendingReturnsHandler;
import com.novashop.orderreturn.application.port.OutboxPort;
import com.novashop.orderreturn.application.port.ReturnRequestRepository;
import com.novashop.orderreturn.domain.actor.ActorRole;
import com.novashop.orderreturn.domain.money.Money;
import com.novashop.orderreturn.domain.returnrequest.ReturnRequest;
import com.novashop.platform.events.RefundTerminal;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class ApplicationHandlersConfig {

  @Bean
  ApproveReturnHandler approveReturnHandler(
      ReturnRequestRepository repository,
      OutboxPort outbox,
      @Value("${novashop.agent-approve-limit:500.00}") String agentLimit) {
    return new ApproveReturnHandler(repository, outbox, Money.cny(agentLimit));
  }

  @Bean
  CreateReturnHandler createReturnHandler(ReturnRequestRepository repository) {
    return new CreateReturnHandler(repository);
  }

  @Bean
  ListPendingReturnsHandler listPendingReturnsHandler(ReturnRequestRepository repository) {
    return new ListPendingReturnsHandler(repository);
  }

  @Bean
  GetReturnHandler getReturnHandler(ReturnRequestRepository repository) {
    return new GetReturnHandler(repository);
  }

  @Bean
  ApplyRefundTerminalHandler applyRefundTerminalHandler(ReturnRequestRepository repository) {
    return new ApplyRefundTerminalHandler(repository);
  }

  /**
   * Transactional façade so aggregate save and outbox append share one TX without putting Spring
   * onto the domain-pure handler class.
   */
  @Service
  public static class ApproveReturnService {

    private final ApproveReturnHandler handler;

    public ApproveReturnService(ApproveReturnHandler handler) {
      this.handler = handler;
    }

    @Transactional
    public ApproveResult handle(
        String returnId, ApproveReturnCommand cmd, String actorId, ActorRole role) {
      return handler.handle(returnId, cmd, actorId, role);
    }
  }

  @Service
  public static class CreateReturnService {

    private final CreateReturnHandler handler;

    public CreateReturnService(CreateReturnHandler handler) {
      this.handler = handler;
    }

    @Transactional
    public String handle(CreateReturnCommand cmd) {
      return handler.handle(cmd);
    }
  }

  @Service
  public static class ListPendingReturnsService {

    private final ListPendingReturnsHandler handler;

    public ListPendingReturnsService(ListPendingReturnsHandler handler) {
      this.handler = handler;
    }

    @Transactional(readOnly = true)
    public List<ReturnRequest> handle() {
      return handler.handle();
    }
  }

  @Service
  public static class GetReturnService {

    private final GetReturnHandler handler;

    public GetReturnService(GetReturnHandler handler) {
      this.handler = handler;
    }

    @Transactional(readOnly = true)
    public Optional<ReturnRequest> handle(String id) {
      return handler.handle(id);
    }
  }

  @Service
  public static class ApplyRefundTerminalService {

    private final ApplyRefundTerminalHandler handler;

    public ApplyRefundTerminalService(ApplyRefundTerminalHandler handler) {
      this.handler = handler;
    }

    @Transactional
    public void handle(RefundTerminal event) {
      handler.handle(event);
    }
  }
}
