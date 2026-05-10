package com.agenthub.application.command;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatchTenantCommand {
    private /** 租户名称（可选） */ String name;
}
