package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

/**
 * @author huangdayu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EtlCommand {

    private String kbId;
    private String documentId;
    private InputStream inputStream;
    private String contentType;
    private String fileName;

}
