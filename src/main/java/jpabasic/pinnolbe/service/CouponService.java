package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Coupon;
import jpabasic.pinnolbe.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    public List<Coupon> generateCoupons(
            int count,int discount,int validDays,String batchName
    ){
        List<Coupon> coupons=new ArrayList<>();
        for(int i=0;i<count;i++){
            String code;
            do{
                code= RandomStringUtils.randomAlphanumeric(8).toUpperCase();
            }while(couponRepository.existsByCode(code));

            Coupon coupon=Coupon.builder()
                    .code(code)
                    .discountAMount(discount)
                    .used(false)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(validDays))
                    .batchName(batchName)
                    .build();
            coupons.add(coupon);
        }
        couponRepository.saveAll(coupons);
        return coupons;
    }
}
