package au.org.aodn.nrmn.restapi.model.db.audit;

import lombok.*;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.envers.Audited;
import org.springframework.security.core.Authentication;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "user_action_aud")
public class UserActionAudit {
    @SequenceGenerator(name="entity_id_seq", sequenceName="hibernate_sequence", allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private long id;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "username")
    private String username;

    @Column(name = "operation")
    private String operation;

    @Column(name = "details", columnDefinition="text")
    private String details;

    @Column(name = "audit_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp auditTime;

    public UserActionAudit(String operation, String details, Authentication authentication) {
        auditTime = new Timestamp(System.currentTimeMillis());
        this.operation = operation;
        this.username = authentication.getName();
        this.requestId = ThreadContext.get("requestId");
        this.details = details;
    }

    public UserActionAudit(String operation, String details, String username) {
        auditTime = new Timestamp(System.currentTimeMillis());
        this.operation = operation;
        this.username = username;
        this.requestId = ThreadContext.get("requestId");
        this.details = details;
    }

    public UserActionAudit(String operation, String details) {
        this(operation, details, getContext().getAuthentication());
    }
}
