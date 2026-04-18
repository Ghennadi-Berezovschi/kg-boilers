package com.kgboilers.dto.centralheatingquote;

import com.kgboilers.model.centralheatingquote.enums.InstallationItemType;
import lombok.Data;

@Data
public class InstallationItemRequestDto {

    private InstallationItemType installationItemType;
}
