package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.centralheatingquote.enums.RadiatorIssueType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class RadiatorIssuesRequestDto {

    @NotEmpty(message = "Please select at least one radiator issue")
    private Set<RadiatorIssueType> radiatorIssues;

    private String otherIssueDetails;
}
