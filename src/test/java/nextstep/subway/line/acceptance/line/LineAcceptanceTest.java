package nextstep.subway.line.acceptance.line;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import nextstep.subway.line.acceptance.documentation.LineDocumentation;
import nextstep.subway.line.acceptance.documentation.LineFindDocumentation;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.station.dto.StationResponse;
import nextstep.subway.utils.AcceptanceTest;
import nextstep.subway.utils.BaseDocumentSteps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.RestDocumentationContextProvider;

import java.util.Arrays;

import static nextstep.subway.line.acceptance.line.LineRequestSteps.*;
import static nextstep.subway.line.acceptance.line.LineVerificationSteps.*;
import static nextstep.subway.station.acceptance.StationRequestSteps.지하철_역_등록_됨;
import static nextstep.subway.utils.BaseDocumentSteps.givenDefault;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    private static final String DOCUMENT_IDENTIFIER_LINE = "line/{method-name}";

    private StationResponse 강남역;
    private StationResponse 양재역;
    private LineRequest 신분당선_강남_양재_노선_요청;

    private StationResponse 역삼역;
    private LineRequest 이호선_강남_역삼_노선_요청;

    private BaseDocumentSteps baseDocumentSteps;

    @BeforeEach
    public void init(RestDocumentationContextProvider restDocumentation) {
        super.setUp(restDocumentation);

        // given
        강남역 = 지하철_역_등록_됨("강남역").as(StationResponse.class);
        양재역 = 지하철_역_등록_됨("양재역").as(StationResponse.class);

        신분당선_강남_양재_노선_요청 = new LineRequest("신분당선", "bg-red-600", 강남역.getId(), 양재역.getId(), 5, 5);

        역삼역 = 지하철_역_등록_됨("역삼역").as(StationResponse.class);
        이호선_강남_역삼_노선_요청 = new LineRequest("2호선", "bg-green-600", 강남역.getId(), 역삼역.getId(), 5, 5);
    }

    @Test
    @DisplayName("지하철 노선을 생성한다.")
    void createLine() {
        // given
        baseDocumentSteps = new LineDocumentation(spec);
        RequestSpecification 지하철_노선_생성_문서화_요청 = baseDocumentSteps.requestDocumentOfAllType(DOCUMENT_IDENTIFIER_LINE);

        // when
        ExtractableResponse<Response> createLineResponse = 지하철_노선_생성_요청(지하철_노선_생성_문서화_요청, 신분당선_강남_양재_노선_요청);

        // then
        지하철_노선_생성_됨(createLineResponse);
    }

    @Test
    @DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성한다.")
    void createLineWithDuplicationName() {
        // given
        지하철_노선_생성_요청(givenDefault(), 신분당선_강남_양재_노선_요청);

        // when
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(givenDefault(), 신분당선_강남_양재_노선_요청);

        // then
        지하철_노선_생성_실패_됨(response);
    }

    @Test
    @DisplayName("지하철 노선 목록을 조회한다.")
    void getLines() {
        // given
        baseDocumentSteps = new LineDocumentation(spec);
        RequestSpecification 지하철_노선_목록_조회_문서화_요청 = baseDocumentSteps.requestDocumentOfDefault(DOCUMENT_IDENTIFIER_LINE);

        ExtractableResponse<Response> 신분당선_강남_양재_구간 = 지하철_노선_생성_요청(givenDefault(), 신분당선_강남_양재_노선_요청);
        ExtractableResponse<Response> 이호선_강남_역삼_구간 = 지하철_노선_생성_요청(givenDefault(), 이호선_강남_역삼_노선_요청);

        // when
        ExtractableResponse<Response> readLinesResponse = 지하철_노선_목록_조회_요청(지하철_노선_목록_조회_문서화_요청);

        // then
        지하철_노선_목록_조회_됨(readLinesResponse);
        지하철_노선_목록_조회_결과에_생성된_노선_ID_포함_확인(readLinesResponse, Arrays.asList(신분당선_강남_양재_구간, 이호선_강남_역삼_구간));
    }

    @Test
    @DisplayName("지하철 노선을 조회한다.")
    void getLine() {
        // given
        baseDocumentSteps = new LineFindDocumentation(spec);
        RequestSpecification 지하철_노선_조회_문서화_요청 = baseDocumentSteps.requestDocumentOfDefault(DOCUMENT_IDENTIFIER_LINE);

        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(givenDefault(), 신분당선_강남_양재_노선_요청);

        // when
        ExtractableResponse<Response> readLineResponse = 지하철_노선_조회_요청(지하철_노선_조회_문서화_요청, 생성된_지하철_노선_ID(createResponse));

        // then
        지하철_노선_조회_됨(readLineResponse);
    }

    @Test
    @DisplayName("지하철 노선을 수정한다.")
    void updateLine() {
        // given
        baseDocumentSteps = new LineDocumentation(spec);
        RequestSpecification 지하철_노선_수정_문서화_요청 = baseDocumentSteps.requestDocumentOfDefault(DOCUMENT_IDENTIFIER_LINE);

        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(givenDefault(), 신분당선_강남_양재_노선_요청);

        // when
        ExtractableResponse<Response> updateResponse = 지하철_노선_수정_요청(지하철_노선_수정_문서화_요청, 생성된_지하철_노선_ID(createResponse), "구분당선", "bg-blue-600");

        // then
        지하철_노선_수정_됨(updateResponse);
    }

    @Test
    @DisplayName("존재하지 않는 노선을 수정한다.")
    void updateNonExistLine() {
        // when
        ExtractableResponse<Response> updateResponse = 지하철_노선_수정_요청(givenDefault(), 100L, "구분당선", "bg-blue-600");

        // then
        지하철_노선_수정_실패_됨(updateResponse);
    }

    @Test
    @DisplayName("지하철 노선을 제거한다.")
    void deleteLine() {
        // given
        baseDocumentSteps = new LineFindDocumentation(spec);
        RequestSpecification 지하철_노선_제거_문서화_요청 = baseDocumentSteps.requestDocumentOfDefault(DOCUMENT_IDENTIFIER_LINE);

        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(givenDefault(), 신분당선_강남_양재_노선_요청);

        // when
        ExtractableResponse<Response> deleteResponse = 지하철_노선_제거_요청(지하철_노선_제거_문서화_요청, 생성된_지하철_노선_ID(createResponse));

        // then
        지하철_노선_제거_됨(deleteResponse);
    }

    @Test
    @DisplayName("존재하지 않는 노선을 제거한다.")
    void deleteNonExistLine() {
        // when
        ExtractableResponse<Response> deleteResponse = 지하철_노선_제거_요청(givenDefault(), 100L);

        // then
        지하철_노선_제거_실패_됨(deleteResponse);
    }
}
