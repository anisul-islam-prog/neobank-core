package com.neobank.transfers.web;

import com.neobank.transfers.TransactionResult;
import com.neobank.transfers.TransferRequest;
import com.neobank.transfers.api.TransferApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Fund transfer operations")
public class TransferController {

    private final TransferApi transferApi;

    public TransferController(TransferApi transferApi) {
        this.transferApi = transferApi;
    }

    @PostMapping
    @Operation(summary = "Execute a fund transfer", description = "Transfer funds from one account to another")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
        @ApiResponse(responseCode = "400", description = "Transfer failed (e.g., insufficient balance)")
    })
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody TransferRequest request) {
        TransactionResult result = transferApi.transfer(request);

        return switch (result) {
            case TransactionResult.Success success ->
                ResponseEntity.ok(Map.of("status", "success", "message", success.message()));
            case TransactionResult.Failure failure ->
                ResponseEntity.badRequest().body(Map.of("status", "failure", "message", failure.errorMessage()));
        };
    }
}
