package uk.gov.dwp.rbc.sp.underpayments.domain.model.awardcomponent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SpSubAwardComponent.class, name = "PE"),
        @JsonSubTypes.Type(value = GmpSubAwcm.class, name = "GMP"),
        @JsonSubTypes.Type(value = Awcm2AwcmLink.class, name = "LINK")
})
public abstract class SubAwardComponent {

    @JsonIgnore
    public abstract String getType();

    private String rawData;

}
