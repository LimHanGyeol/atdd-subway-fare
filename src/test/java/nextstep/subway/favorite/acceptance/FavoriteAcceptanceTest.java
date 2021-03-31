package nextstep.subway.favorite.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.favorite.acceptance.documentation.FavoriteDocumentation;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.utils.AcceptanceTest;
import nextstep.subway.utils.BaseDocumentSteps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;

import java.util.Arrays;

import static nextstep.subway.favorite.acceptance.FavoriteRequestSteps.*;
import static nextstep.subway.favorite.acceptance.FavoriteVerificationSteps.*;
import static nextstep.subway.member.acceptance.MemberRequestSteps.로그인_되어_있음;
import static nextstep.subway.member.acceptance.MemberRequestSteps.회원_생성_요청;
import static nextstep.subway.station.acceptance.StationRequestSteps.지하철_역_등록_됨;
import static nextstep.subway.utils.BaseDocumentSteps.givenDefault;

@DisplayName("지하철 즐겨찾기 관련 기능 인수 테스트")
public class FavoriteAcceptanceTest extends AcceptanceTest {

    private static final String DOCUMENT_IDENTIFIER_FAVORITE = "favorite/{method-name}";

    private static final String EMAIL = "email@email.com";
    private static final String OTHER_EMAIL = "otherEmail@email.com";
    private static final String PASSWORD = "password";
    private static final Integer AGE = 20;

    private StationResponse 강남역;
    private StationResponse 청계산입구역;
    private StationResponse 양재역;

    private TokenResponse 로그인_멤버_토큰 = new TokenResponse("Unauthorized");

    private BaseDocumentSteps baseDocumentSteps;

    @BeforeEach
    void init(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);

        // given
        강남역 = 지하철_역_등록_됨("강남역").as(StationResponse.class);
        청계산입구역 = 지하철_역_등록_됨("청계산입구역").as(StationResponse.class);
        양재역 = 지하철_역_등록_됨("양재역").as(StationResponse.class);

        회원_생성_요청(givenDefault(), EMAIL, PASSWORD, AGE);
        회원_생성_요청(givenDefault(), OTHER_EMAIL, PASSWORD, AGE);
    }

    @Test
    @DisplayName("즐겨찾기 추가")
    void addFavorite() {
        // given
        baseDocumentSteps = new FavoriteDocumentation(spec);
        RequestSpecification 즐겨찾기_추가_문서화_요청 = baseDocumentSteps.requestDocumentOfAllType(DOCUMENT_IDENTIFIER_FAVORITE);

        로그인_멤버_토큰 = 로그인_되어_있음(EMAIL, PASSWORD);

        // when
        ExtractableResponse<Response> response = 지하철_즐겨찾기_추가_요청(즐겨찾기_추가_문서화_요청, 로그인_멤버_토큰, 강남역, 청계산입구역);

        // then
        지하철_즐겨찾기_추가_됨(response);
    }

    @Test
    @DisplayName("이미 존재하는 즐겨찾기를 추가할 경우 Exception 발생")
    void validateAlreadyFavorite() {
        // given
        로그인_멤버_토큰 = 로그인_되어_있음(EMAIL, PASSWORD);
        지하철_즐겨찾기_추가_요청(givenDefault(), 로그인_멤버_토큰, 강남역, 청계산입구역);

        // when
        ExtractableResponse<Response> response = 지하철_즐겨찾기_추가_요청(givenDefault(), 로그인_멤버_토큰, 강남역, 청계산입구역);

        // then
        지하철_즐겨찾기_추가_실패_됨(response);
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자가 즐겨찾기 접근 할 경우 401 에러")
    void unauthorizedAddFavorite() {
        // when
        ExtractableResponse<Response> response = 지하철_즐겨찾기_추가_요청(givenDefault(), 로그인_멤버_토큰, 강남역, 청계산입구역);

        // then & when
        지하철_즐겨찾기_미인증_회원_실패_됨(response);
    }

    @Test
    @DisplayName("즐겨찾기 조회")
    void findFavorites() {
        // given
        baseDocumentSteps = new FavoriteDocumentation(spec);
        RequestSpecification 즐겨찾기_조회_문서화_요청 = baseDocumentSteps.requestDocumentOfDefault(DOCUMENT_IDENTIFIER_FAVORITE);

        로그인_멤버_토큰 = 로그인_되어_있음(EMAIL, PASSWORD);
        지하철_즐겨찾기_추가_요청(givenDefault(), 로그인_멤버_토큰, 강남역, 청계산입구역);
        지하철_즐겨찾기_추가_요청(givenDefault(), 로그인_멤버_토큰, 강남역, 양재역);

        // when
        ExtractableResponse<Response> response = 지하철_즐겨찾기_목록_조회_요청(즐겨찾기_조회_문서화_요청, 로그인_멤버_토큰);

        // then
        지하철_즐겨찾기_조회_됨(response);
        지하철_즐겨찾기_조회_결과_확인(response, Arrays.asList(강남역, 청계산입구역, 양재역));
    }

    @Test
    @DisplayName("즐겨찾기 제거")
    void removeFavorite() {
        // given
        baseDocumentSteps = new FavoriteDocumentation(spec);
        RequestSpecification 즐겨찾기_제거_문서화_요청 = baseDocumentSteps.requestDocumentOfDefault(DOCUMENT_IDENTIFIER_FAVORITE);

        로그인_멤버_토큰 = 로그인_되어_있음(EMAIL, PASSWORD);
        ExtractableResponse<Response> addFavoriteResponse = 지하철_즐겨찾기_추가_요청(givenDefault(), 로그인_멤버_토큰, 강남역, 청계산입구역);

        // when
        ExtractableResponse<Response> response = 지하철_즐겨찾기_제거_요청(즐겨찾기_제거_문서화_요청, 로그인_멤버_토큰, 지하철_즐겨찾기_생성된_ID(addFavoriteResponse));

        // then
        지하철_즐겨찾기_제거_됨(response);
    }

    @Test
    @DisplayName("자신이 등록하지 않은 즐겨찾기 제거시 Exception 발생")
    void validateRemoveOtherFavorite() {
        // given
        로그인_멤버_토큰 = 로그인_되어_있음(EMAIL, PASSWORD);

        TokenResponse 다른_사용자_멤버_토큰 = 로그인_되어_있음(OTHER_EMAIL, PASSWORD);
        ExtractableResponse<Response> addedFavoriteResponse = 지하철_즐겨찾기_추가_요청(givenDefault(), 다른_사용자_멤버_토큰, 강남역, 양재역);

        // when
        ExtractableResponse<Response> response = 지하철_즐겨찾기_제거_요청(givenDefault(), 로그인_멤버_토큰, 지하철_즐겨찾기_생성된_ID(addedFavoriteResponse));

        // then
        지하철_즐겨찾기_제거_실패_됨(response);
    }

    @DisplayName("즐겨찾기를 관리한다.")
    @Test
    void manageMember() {
        // given
        로그인_멤버_토큰 = 로그인_되어_있음(EMAIL, PASSWORD);

        // when
        ExtractableResponse<Response> createResponse = 지하철_즐겨찾기_추가_요청(givenDefault(), 로그인_멤버_토큰, 강남역, 청계산입구역);
        // then
        지하철_즐겨찾기_추가_됨(createResponse);

        // when
        ExtractableResponse<Response> findResponse = 지하철_즐겨찾기_목록_조회_요청(givenDefault(), 로그인_멤버_토큰);
        // then
        지하철_즐겨찾기_조회_됨(findResponse);

        // when
        ExtractableResponse<Response> deleteResponse = 지하철_즐겨찾기_제거_요청(givenDefault(), 로그인_멤버_토큰, 지하철_즐겨찾기_생성된_ID(createResponse));
        // then
        지하철_즐겨찾기_제거_됨(deleteResponse);
    }
}
