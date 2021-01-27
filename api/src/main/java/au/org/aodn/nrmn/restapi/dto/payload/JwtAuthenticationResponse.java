package au.org.aodn.nrmn.restapi.dto.payload;

import au.org.aodn.nrmn.restapi.model.db.SecRole;
import au.org.aodn.nrmn.restapi.model.db.enums.SecRoleName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    private String username;
    private Set<SecRole> roles;

    public JwtAuthenticationResponse(String accessToken, String username, Set<SecRole> roles) {
        this.accessToken = accessToken;
        this.username = username;
        this.roles = roles;
    }

}
