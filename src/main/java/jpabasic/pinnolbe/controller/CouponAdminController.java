package jpabasic.pinnolbe.controller;

import com.google.common.net.HttpHeaders;
import jpabasic.pinnolbe.domain.Coupon;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/coupon")
public class CouponAdminController {

    private final CouponService couponService;

    @PostMapping("/generate")
    public ResponseEntity<Resource> generateCoupons(
            @RequestParam int count,
            @RequestParam int discount,
            @RequestParam int validDays,
            @RequestParam(defaultValue = "preorder-2025-11") String batchName
    ){
        List<Coupon> coupons=couponService.generateCoupons(count,discount,validDays,batchName);

        StringBuilder csv=new StringBuilder("code,discount,expiresAt,batchname\n");
        for(Coupon c:coupons){
            csv.append(c.getCode()).append(",")
                    .append(c.getDiscountAMount()).append(",")
                    .append(c.getExpiresAt()).append(",")
                    .append(c.getBatchName()).append("\n");
        }

        ByteArrayResource resource=new ByteArrayResource(csv.toString().getBytes());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=coupons.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

}
