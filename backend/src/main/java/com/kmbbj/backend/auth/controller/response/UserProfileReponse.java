package com.kmbbj.backend.auth.controller.response;

import com.kmbbj.backend.balance.controller.AssetTransactionresponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** ToDo
 * Rank 연결해야함
 */
@Schema(description = "profile 페이지에 사용할 Dto, 사용자정보, 최근 순위, 내 자산 현황을 보여준다.")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileReponse {
    @Schema(description = "사용자 이름", example = "김창섭")
    private String nickName;

    @Schema(description = "이메일", example = "chang@gmail.com")
    private String email;

    @Schema(description = "자산 현황", example = "500")
    private Long asset;

    @Schema(description = "자산 변동 내역 리스트")
    private List<AssetTransactionresponse> assetTransactionList;
//    private List<Rank>;
}