package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class OperationResult implements Serializable {
    private String payload;
    private boolean success;
}
