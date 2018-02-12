<%@ page import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- 위 3개의 메타 태그는 *반드시* head 태그의 처음에 와야합니다; 어떤 다른 콘텐츠들은 반드시 이 태그들 *다음에* 와야 합니다 -->
<title>부트스트랩 101 템플릿</title>

<!-- 합쳐지고 최소화된 최신 CSS -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">

<!-- 부가적인 테마 -->
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">

<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<!-- 합쳐지고 최소화된 최신 자바스크립트 -->
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
<style type="text/css">
.resize-100x100 {
    height: 100px;
    width: 100px;
}
#img_container {
     width:100%;
     height:100%;
}
 
#img_container img {
    display:block;
    margin-left:auto;
    margin-right:auto;
}
</style>
</head>
<body>
	<nav class="navbar navbar-default">
	<div class="container-fluid">
		<!-- Brand and toggle get grouped for better mobile display -->
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed"
				data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="/helm/chartList">Helm Chart</a>
		</div>

		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse"
			id="bs-example-navbar-collapse-1">
			<ul class="nav navbar-nav">
				<li><a href="/helm/chartList">Charts<span class="sr-only">(current)</span></a></li>
				<li class="active"><a href="/helm/deploymentList">Deployments <span class="sr-only">(current)</span></a></li>
		</div>
	</div>
	</nav>
	<div class="panel panel-default">
		<div class="panel-body">
			<div class="row">
				<div class="col-md-12">
			  		<div class="panel panel-default">
				  		<div class="page-header">
					  		<h2>Deployments LIST</h2>
						</div>
						<div id="img_container" class="panel-body">
					  		<c:forEach items="${DEP_LIST}" var="dep">
							<div class="panel panel-primary">
							<div class="panel-heading">Deployment 목록</div>
								<table class="table">
						        <thead>
						          <tr>
						            <th>#</th>
						            <th>Chart Name</th>
						            <th>Dep. Name</th>
						            <th>Dep. Version</th>
						            <th>Dep. Namespace</th>
						            <th>Dep. Status</th>
						            <th>Dep. FirstDay</th>
						            <th>Dep. LastDay</th>
						            <th>Dep. Detail</th>
						          </tr>
						        </thead>
						        <tbody>
									<c:forEach items="${dep}" var="res" varStatus="status">
							          <tr>
							            <th scope="row">${status.count}</th>
							            <td>${res['CHART_NAME'] }</td>
							            <td>${res['DEP_NAME'] }</td>
							            <td>${res['DEP_CHART_VERSION'] }</td>
							            <td>${res['DEP_NAMESPACE'] }</td>
							            <td>${res['DEP_STATUS'] }</td>
							            <td>${res['DEP_FIRST'] }</td>
							            <td>${res['DEP_LAST'] }</td>
							            <td>
							            	<a href="/helm/deploymentDetail/${res['DEP_NAME'] }" class="btn btn-primary" role="button">Detail</a>
							            	<%-- <a href="/helm/chartDelete/${res['DEP_NAME'] }" class="btn btn-primary" role="button">Delete</a> --%>
							            </td>
							          </tr>
									</c:forEach>
									</tbody>
								</table>
							</div>
							</c:forEach>
					  	</div>
				  	</div>
				</div>
				
				
			</div>
		</div>
		<nav class="navbar navbar-default navbar-fixed-bottom">
		<div class="container">...</div>
		</nav>
</body>
</html>