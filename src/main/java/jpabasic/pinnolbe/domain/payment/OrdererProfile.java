package jpabasic.pinnolbe.domain.payment;

import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jpabasic.pinnolbe.domain.BaseEntity;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.payment.OrdererEditReqDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection="orderer_profile")
@Schema(description="주문자 정보")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdererProfile extends BaseEntity {

	@Id
	private String id;

	private String userId;

	@Column(name="orderer_name")
	private String ordererName;

	@Column(name="orderer_phone")
	private String ordererPhone;

	@Column(name="orderer_email")
	private String ordererEmail;

	public static OrdererProfile create(User user){
		OrdererProfile ordererProfile = new OrdererProfile();
		ordererProfile.setUserId(user.getId());
		ordererProfile.setOrdererName(user.getName());
		ordererProfile.setOrdererPhone(user.getPhoneNumber());
		ordererProfile.setOrdererEmail(user.getEmail());
		return ordererProfile;
	}

	public void update(OrdererEditReqDto request) {

		if (request.name() != null) {
			this.ordererName = request.name();
		}

		if (request.phone() != null) {
			this.ordererPhone = request.phone();
		}

		if (request.email() != null) {
			this.ordererEmail = request.email();
		}
	}

}
