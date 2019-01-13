<%@ page import="com.rancre.config.GlobalValue"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<jsp:include page="/02_page/commonHeader.jsp" flush="true" />
<script>

function joinPermit() {
	var inputMemberUid = "${memberUid}";
	
	if(inputMemberUid=="") {
		alert("잘못된 접근입니다.");	
		return;
	}
	
	var state1 = $('#commercial-category option:selected').val();
	var state2 = $('#commercial-target-age option:selected').val();
	var state3 = $('#commercial-target-sex option:selected').val();

	if(!(state1 >= 0)) {
		alert("광고 할 상품과 가장 유사한 카테고리를 선택해주세요. 채널 추천에 도움이 됩니다.");
		return;
	}
	
	if(!(state2 >= 0)) {
		alert("광고 할 상품의 연령을 선택해주세요. 채널 추천에 도움이 됩니다.");
		return;
	}
	
	
	if(!(state3 >= 0)) {
		alert("광고 할 상품의 성별을 선택해주세요. 채널 추천에 도움이 됩니다.");
		return;
	}
		
	var inputAdCategory = $('#commercial-category option:selected').val();
	var inputTargetAge = $('#commercial-target-age option:selected').val();
	var inputTargetSex = $('#commercial-target-sex option:selected').val();

	form_data = {
			inputAdCategory : inputAdCategory,
			inputTargetAge : inputTargetAge,
			inputTargetSex : inputTargetSex,
			inputMemberUid : inputMemberUid
	};
	
	$.ajax({
		type : "POST",
		url : "/member?action=addMemberAdTarget",
		dataType : "json",
		data : form_data,
		async: false,
		success : function(response){
			if(response.outputResult == "1"){
				if (confirm("지금 광고 리뷰를 입력하시겠습니까? 리뷰를 입력하시면 다른 사람의 리뷰를 모두 확인할 수 있습니다.") == true){    //확인
					// 리뷰 작성 페이지로
					location.href = "/02_page/Review/review.jsp";
				}else{   //취소
					location.href = "/02_page/Auth/login.jsp";
				}

			}else if(response.outputResult == "-1"){

			}
		}, error: function(xhr,status,error) {
			alert(error);
		}
	});
}

</script>
</head>
<body>

	<div class="container">
		<div class="row">
			<div class="col txt-center">
				<div class="txt-center join-Permit">

					<div class="v20"></div>

					<div class="join-Permit-Brand">Rancre</div>

					<div class="pTop">
						<span>랭크리에 가입한 것을 환영합니다! </br> 아래 설문을 완료하면 바로 서비스 이용이 가능합니다.
						</span>
					</div>

					<div class="ipt-joinPermit pTop3">
						<div class="float-left joinPermit-commonTxt inline-block">
							채널에 광고하고자 하는 상품이 있나요?<span class="primary-color pRight">*</span>
						</div>
						<div class="float-left joinPermit-commonSubTxt inline-block">정확한
							계획은 없지만, 유튜브 채널을 통해 광고하고 싶은 물품이 있나요?</div>
						<div class="inline-block">
							<select class="ipt-Select-Join" id="commercial-category" required>
								<option disabled selected hidden>광고할 상품과 제일 근접한 카테고리를
									선택해주세요!</option>
								<option value="0">1</option>
								<option value="1">2</option>
								<option value="2">3</option>
								<option value="3">4</option>
								<option value="4">5</option>
							</select>
						</div>
					</div>

					<div class="w100 pTop2">
						<div class="joinPermit-commonTxt float-left inline-block">
							상품의 타깃 연령은 어떻게 되나요?<span class="primary-color pRight10">*</span>
						</div>
						<div class="float-left joinPermit-commonSubTxt inline-block">광고하고자
							하는 상품의 타깃 연령을 선택해주세요.</div>
						<div class="inline-block">
							<select class="ipt-Select-Join" id="commercial-target-age">
								<option disabled selected hidden>연령대를 선택해주세요!</option>
								<option value="0">연령무관</option>
								<option value="1">10대</option>
								<option value="2">10대 - 20대</option>
								<option value="3">20대</option>
								<option value="4">20대 - 30대</option>
								<option value="5">30대</option>
								<option value="6">30대 - 40대</option>
								<option value="7">40대</option>
								<option value="8">40대 - 50대</option>
								<option value="9">50대 이상</option>
							</select>
						</div>
					</div>

					<div class="ipt-joinPermit pTop2">

						<div class="float-left joinPermit-commonTxt inline-block">
							상품의 타깃 성별은 어떻게 되나요?<span class="primary-color pRight10">*</span>
						</div>
						<div class="float-left joinPermit-commonSubTxt inline-block">광고하고자
							하는 상품의 타깃 성별을 선택해주세요.</div>
						<div class="inline-block">
							<select class="ipt-Select-Join" id="commercial-target-sex">
								<option disabled selected hidden>연령대를 선택해주세요!</option>
					
								<option value="0">성별무관</option>
								<option value="1">남성</option>
								<option value="2">여성</option>
							</select>
						</div>

					</div>

					<div class="ipt-joinPermit pTop2">

						<div class="float-left joinPermit-commonTxt inline-block">
							동영상 채널을 통해 광고를 하신적이 있나요?<span class="primary-color pRight10">*</span>
						</div>
					</div>
					<div class="float-right">
						<div class="radio-item">
							<input type="radio" id="commercial-exYes" name="commercial-ex"
								value="1" checked> <label for="commercial-exYes">예</label>
						</div>

						<div class="radio-item">
							<input type="radio" id="commercial-exNo" name="commercial-ex"
								value="0"> <label for="commercial-exNo">아니오</label>
						</div>
					</div>

					<div class="pTop3"></div>

					<div class="pTop3">
						<a href="#" onclick="joinPermit()"><button
								class="common-wide-Btn" type="button">회원가입 완료</button></a>
					</div>

						<div class="pTop4"></div>

					<div class="float-left">
						<span class="brand-color">Rancre</span><span class="gray-color">
							all right reserved.</span>
					</div>
					<div class="float-right login-Redirect">
						<a href="#">Contact Us</a>
					</div>

					<div class="pTop4"></div>

				</div>
			</div>
		</div>
	</div>


</body>
</html>