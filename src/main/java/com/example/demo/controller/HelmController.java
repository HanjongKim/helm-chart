package com.example.demo.controller;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assume.assumeNoException;

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONObject;
import org.microbean.helm.ReleaseManager;
import org.microbean.helm.chart.URLChartLoader;
import org.microbean.helm.chart.repository.ChartRepository;
import org.microbean.helm.chart.repository.ChartRepository.Index.Entry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.logging.type.HttpRequest;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;

import hapi.chart.ChartOuterClass.Chart;
import hapi.chart.ConfigOuterClass.Config;
import hapi.chart.MetadataOuterClass.MetadataOrBuilder;
import hapi.chart.TemplateOuterClass.Template;
import hapi.chart.TemplateOuterClass.Template.Builder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import net.minidev.json.JSONValue;
import hapi.release.ReleaseOuterClass.Release;
import hapi.services.tiller.Tiller.GetReleaseStatusRequest;
import hapi.services.tiller.Tiller.GetReleaseStatusResponse;
import hapi.services.tiller.Tiller.InstallReleaseRequest;
import hapi.services.tiller.Tiller.InstallReleaseResponse;
import hapi.services.tiller.Tiller.ListReleasesRequest;
import hapi.services.tiller.Tiller.ListReleasesResponse;
import hapi.services.tiller.Tiller.UninstallReleaseRequest;
import hapi.services.tiller.Tiller.UninstallReleaseResponse;

import org.microbean.helm.Tiller;

@Controller
@RequestMapping(path = "/helm")
public class HelmController {

	@Value("${helm.repo.url}")
	String helmRepoUrl;
	
	@Value("${helm.repo.name}")
	String helmRepoName;
	
	@RequestMapping(path = "/chartList", method = RequestMethod.GET)
	public ModelAndView helmChartList() throws Exception {
		System.out.println("chartList START");
		ModelAndView mav = new ModelAndView();
		
		//URI uri = new URI("https://raw.githubusercontent.com/funnylab/helm-repo/master/");
		URI uri = new URI(helmRepoUrl);
		
		ChartRepository repo = new ChartRepository(helmRepoName, uri);
		repo.clearIndex();
		repo.downloadIndex();
		Map<String, SortedSet<Entry>> entries = repo.getIndex().getEntries();
		Iterator<String> it = entries.keySet().iterator();

		List<HashMap<String, String>> chartList = new ArrayList<>();
		while (it.hasNext()) {
			
			HashMap<String, String> chart = new HashMap<>();
			
			String key = it.next();
			chart.put("ENT_NAME", key); 
			System.out.println("ENTRIES : " + key);
			SortedSet<Entry> ss = entries.get(key);
			Iterator<Entry> entIt = ss.iterator();
			
			//첫번째 차트정보만 가져옴
			//while (entIt.hasNext()) {
				Entry ent = entIt.next();
				MetadataOrBuilder metadata = ent.getMetadataOrBuilder();
				chart.put("CHART_NAME", metadata.getName());
				chart.put("CHART_VERSION", metadata.getVersion());
				chart.put("CHART_APP_VERSION", metadata.getAppVersion());
				chart.put("CHART_DESC", metadata.getDescription());
				chart.put("CHART_ICON", metadata.getIcon());
				chartList.add(chart);
			//}

		}

		mav.addObject("REPO", helmRepoName);
		mav.addObject("CHART_LIST", chartList);
		mav.setViewName("chartList");
		System.out.println("chartList END : " + chartList.size()+"");
		return mav;
	}

	@RequestMapping(path = "/chartDetail/{chartName}/{version:.+}", method = RequestMethod.GET)
	public ModelAndView helmChartDetail(@PathVariable String chartName, @PathVariable String version) throws Exception {
		System.out.println("chartDetail START");
		ModelAndView mav = new ModelAndView();
		
		//String ads = "https://raw.githubusercontent.com/funnylab/helm-repo/master/";
		//String ads = "https://kubernetes-charts-incubator.storage.googleapis.com/";
		
		URI uri = new URI(helmRepoUrl);
		
		ChartRepository repo = new ChartRepository(helmRepoName, uri);
		repo.clearIndex();
		repo.downloadIndex();
		Map<String, SortedSet<Entry>> entries = repo.getIndex().getEntries();
		Iterator<String> it = entries.keySet().iterator();

		SortedSet<Entry> ss = entries.get(chartName);
		Iterator<Entry> entIt = ss.iterator();
		
		URL pkgUri = null;
		List<String> chartVersions = new ArrayList<>(); 
		HashMap<String, String> chartDetail = new HashMap<>();
		while (entIt.hasNext()) {
			Entry ent = entIt.next();
			//pkgUri = new URL(ads + ent.getFirstUri().toString());
			MetadataOrBuilder metadata = ent.getMetadataOrBuilder();
			chartVersions.add(ent.getVersion());
			System.out.println(ent.getVersion() + ":" + version);
			if(ent.getVersion().equals(version)) {
				
				chartDetail.put("CHART_NAME", metadata.getName());
				chartDetail.put("CHART_VERSION", metadata.getVersion());
				chartDetail.put("CHART_APP_VERSION", metadata.getAppVersion());
				chartDetail.put("CHART_DESC", metadata.getDescription());
				chartDetail.put("CHART_ICON", metadata.getIcon());
				
				if(ent.getFirstUri().toString().startsWith("http")){
					pkgUri = new URL(ent.getFirstUri().toString());
				}else {
					pkgUri = new URL(helmRepoUrl + ent.getFirstUri().toString());
				}
			}
		}

		Chart.Builder chart = null;
		try (final URLChartLoader chartLoader = new URLChartLoader()) {
			chart = chartLoader.load(pkgUri);
		}
		
		//Map<FieldDescriptor, Object> field = chart.getAllFields();
		List<Any.Builder> tmpList = chart.getFilesBuilderList();

		for(Any.Builder tmp : tmpList) {
			if("README.md".equals(tmp.getTypeUrl())){
				Parser parser = Parser.builder().build();
				Node document = parser.parse(tmp.getValue().toStringUtf8());
				HtmlRenderer renderer = HtmlRenderer.builder().build();
				mav.addObject("readMe",renderer.render(document));
			}
		}
		
		mav.addObject("CHART_DETAIL", chartDetail);
		mav.addObject("CHART_VERSIONS", chartVersions);
		mav.setViewName("chartDetail");
		System.out.println("chartDetail END");
		return mav;
	}
	
	@RequestMapping(path = "/chartInstallDetail/{chartName}/{version:.+}", method = RequestMethod.GET)
	public ModelAndView helmChartInstallDetail(@PathVariable String chartName, @PathVariable String version) throws Exception {

		System.out.println("chartDetail START");
		ModelAndView mav = new ModelAndView();
		
		//String ads = "https://raw.githubusercontent.com/funnylab/helm-repo/master/";
		//String ads = "https://kubernetes-charts-incubator.storage.googleapis.com/";
		
		URI uri = new URI(helmRepoUrl);
		
		ChartRepository repo = new ChartRepository(helmRepoName, uri); 
		repo.clearIndex();
		repo.downloadIndex();
		Map<String, SortedSet<Entry>> entries = repo.getIndex().getEntries();
		Iterator<String> it = entries.keySet().iterator();

		SortedSet<Entry> ss = entries.get(chartName);
		Iterator<Entry> entIt = ss.iterator();

		URL pkgUri = null;
		List<String> chartVersions = new ArrayList<>(); 
		HashMap<String, String> chartDetail = new HashMap<>();
		while (entIt.hasNext()) {
			Entry ent = entIt.next();
			System.out.println(ent.getVersion() + ":" + version);
			
			MetadataOrBuilder metadata = ent.getMetadataOrBuilder();
			chartVersions.add(ent.getVersion());
			System.out.println(ent.getVersion() + ":" + version);
				
			if(ent.getVersion().equals(version)) {
				
				chartDetail.put("CHART_NAME", metadata.getName());
				chartDetail.put("CHART_VERSION", metadata.getVersion());
				chartDetail.put("CHART_APP_VERSION", metadata.getAppVersion());
				chartDetail.put("CHART_DESC", metadata.getDescription());
				chartDetail.put("CHART_ICON", metadata.getIcon());

				if(ent.getFirstUri().toString().startsWith("http")){
					pkgUri = new URL(ent.getFirstUri().toString());
				}else {
					pkgUri = new URL(helmRepoUrl + ent.getFirstUri().toString());
				}
			}
		}

		Chart.Builder chart = null;
		try (final URLChartLoader chartLoader = new URLChartLoader()) {
			chart = chartLoader.load(pkgUri);
		}
		
		Config.Builder builder = chart.getValuesBuilder();
		Config config = builder.build();
		Yaml yml = new Yaml();
		Map<String, Object> values = (Map<String, Object>) yml.load(config.getRaw());
		
		Map<String, String> valueMap = getValues("",values);

		List<String> keyList = new ArrayList(valueMap.keySet());
		
		mav.addObject("CHART_DETAIL", chartDetail);
		mav.addObject("CHART_VERSIONS", chartVersions);
		mav.addObject("VALUES", valueMap);
		mav.addObject("KEY_LIST", keyList);
		mav.setViewName("chartInstallDetail");
		
		return mav;
	}
	
	public Map<String, String> getValues(String rootKey, Map<String, Object> map){
		Map<String, String> resultMap = new LinkedHashMap<>();
		Iterator<String> it = map.keySet().iterator();
		
		while(it.hasNext()) {
			String key = it.next();
			Object obj = map.get(key);
			
			if(obj instanceof String || obj instanceof Integer || obj instanceof Boolean) {
				System.out.println("aaaaa");
				Map<String, String > prot = new LinkedHashMap<>();
				prot.put(rootKey + key, obj.toString());
				resultMap.put(rootKey+key,obj.toString());
			}else if(obj instanceof LinkedHashMap<?, ?>) {
				System.out.println("vvvv");
				resultMap.putAll(getValues(rootKey + key + ".", (Map<String, Object>)obj));
			}
		}
		return resultMap;
	}
	
	@RequestMapping(path = "/chartInstall/{chartName}/{version:.+}", method = RequestMethod.POST)
	public String helmChartInstall(HttpServletRequest request, @PathVariable String chartName, @PathVariable String version) throws Exception {

		System.out.println("chartDetail START");
		//ModelAndView mav = new ModelAndView();

		String namespace = request.getParameter("namespace") == null?"default":request.getParameter("namespace");
		String deployName = chartName+"-"+ RandomStringUtils.randomAlphanumeric(10).toLowerCase();

		Map<String, hapi.chart.ConfigOuterClass.Value> values = new HashMap<String, hapi.chart.ConfigOuterClass.Value>();
		Enumeration<String> param = request.getParameterNames();
		while(param.hasMoreElements()) {
			String key = param.nextElement();
			if(!key.equals("namespace")) {
				System.out.println("ADD VALUE : " + key + ":" + request.getParameter(key));
				values.put(key, hapi.chart.ConfigOuterClass.Value.newBuilder().setValue(request.getParameter(key)).build());
			}
		}
		
		//String ads = "https://raw.githubusercontent.com/funnylab/helm-repo/master/";
		//String ads = "https://kubernetes-charts-incubator.storage.googleapis.com/";
		
		URI uri = new URI(helmRepoUrl);
		
		ChartRepository repo = new ChartRepository(helmRepoName, uri);
		repo.clearIndex();
		repo.downloadIndex();
		Map<String, SortedSet<Entry>> entries = repo.getIndex().getEntries();
		Iterator<String> it = entries.keySet().iterator();

		SortedSet<Entry> ss = entries.get(chartName);
		Iterator<Entry> entIt = ss.iterator();

		URL pkgUri = null;
		while (entIt.hasNext()) {
			Entry ent = entIt.next();
			
			if(ent.getVersion().equals(version)) {
				
				if(ent.getFirstUri().toString().startsWith("http")){
					pkgUri = new URL(ent.getFirstUri().toString());
				}else {
					pkgUri = new URL(helmRepoUrl + ent.getFirstUri().toString());
				}
			}
		}
		
		Chart.Builder chart = null;
		try (final URLChartLoader chartLoader = new URLChartLoader()) {
			chart = chartLoader.load(pkgUri);
		}

		try (final DefaultKubernetesClient client = new DefaultKubernetesClient();
				final Tiller tiller = new Tiller(client);
				final ReleaseManager releaseManager = new ReleaseManager(tiller)) {

			final InstallReleaseRequest.Builder requestBuilder = InstallReleaseRequest.newBuilder();
			assert requestBuilder != null;
			requestBuilder.setTimeout(300L);
			requestBuilder.setName(deployName); // Set the Helm release name
			requestBuilder.setWait(true); // Wait for Pods to be ready
			requestBuilder.setNamespace(namespace);
			Config.Builder config = requestBuilder.getValuesBuilder();
			config.putAllValues(values);
			// Config.Builder config = Config.newBuilder();
			// requestBuilder.setValues(config);
			// Install the loaded chart with no user-supplied overrides.
			// To override any values, call the requestBuilder.getValuesBuilder() method,
			// and add values to the resulting Builder.

			final Future<InstallReleaseResponse> releaseFuture = releaseManager.install(requestBuilder, chart);
			assert releaseFuture != null;
			System.out.println(releaseFuture.toString());
			final Release release = releaseFuture.get().getRelease();
			assert release != null;
			System.out.println(release.toString());
		}

		//mav.setViewName("forward:/helm/deploymentDetail/"+deployName);
		return "forward:/helm/deploymentDetail/"+deployName;
	}

	@RequestMapping(path = "/deploymentList")
	public ModelAndView helmChartDeploymentList() throws Exception {

		ModelAndView mav = new ModelAndView();
		
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		List<List<Map<String, String>>> depList = new ArrayList<>();
		
		try (final DefaultKubernetesClient client = new DefaultKubernetesClient();
				final Tiller tiller = new Tiller(client);
				final ReleaseManager releaseManager = new ReleaseManager(tiller)) {

			final ListReleasesRequest.Builder requestBuilder = ListReleasesRequest.newBuilder();
			assert requestBuilder != null;

			//requestBuilder.setNamespace("default");

			final Iterator<ListReleasesResponse> releaseFuture = releaseManager.list(requestBuilder.build());

			assert releaseFuture != null;
			// System.out.println(releaseFuture.toString());
			
			while (releaseFuture.hasNext()) {
				List<Map<String, String>> releaseInfos = new ArrayList<>();
				
				ListReleasesResponse ent = releaseFuture.next();
				System.out.println(ent.getReleasesCount());
				System.out.println(ent.getTotal());
				List<Release> releaseList = ent.getReleasesList();

				for(Release release : releaseList) {
					Map<String, String> releaseInfo = new HashMap<>();
					releaseInfo.put("CHART_NAME", release.getChart().getMetadata().getName());
					releaseInfo.put("DEP_NAME", release.getName());
					releaseInfo.put("DEP_CHART_VERSION", release.getChart().getMetadata().getVersion());
					releaseInfo.put("DEP_NAMESPACE", release.getNamespace());
					releaseInfo.put("DEP_STATUS", release.getInfo().getStatus().getCode().toString());
					releaseInfo.put("DEP_FIRST", transFormat.format(new Date(release.getInfo().getFirstDeployed().getSeconds())));
					releaseInfo.put("DEP_LAST", transFormat.format(new Date(release.getInfo().getLastDeployed().getSeconds())));

					releaseInfos.add(releaseInfo);
				}
				depList.add(releaseInfos);
			}
			
			// assert release != null;
			// System.out.println(release.toString());
		}
		mav.addObject("DEP_LIST", depList);
		mav.setViewName("deploymentList");
		return mav;
	}

	@RequestMapping(path = "/deploymentDetail/{depName}")
	public ModelAndView helmChartDeploymentDetail(@PathVariable String depName) throws Exception {

		ModelAndView mav = new ModelAndView();

		Map<String, String> depDetail = new HashMap<>();

		try (final DefaultKubernetesClient client = new DefaultKubernetesClient();
				final Tiller tiller = new Tiller(client);
				final ReleaseManager releaseManager = new ReleaseManager(tiller)) {

			// final ListReleasesRequest.Builder requestBuilder =
			// ListReleasesRequest.newBuilder();
			final GetReleaseStatusRequest.Builder requestBuilder = GetReleaseStatusRequest.newBuilder();
			assert requestBuilder != null;

			// requestBuilder.setNamespace("default");
			requestBuilder.setName(depName);

			final Future<GetReleaseStatusResponse> releaseFuture = releaseManager.getStatus(requestBuilder.build());
			// final Iterator<ListReleasesResponse> releaseFuture =
			// releaseManager.list(requestBuilder.build());

			assert releaseFuture != null;
			// System.out.println(releaseFuture.toString());

			GetReleaseStatusResponse releaseInfo = releaseFuture.get();
			System.out.println(releaseInfo.toString());
			depDetail.put("DEP_NAME", releaseInfo.getName());
			depDetail.put("DEP_NAMESPACE", releaseInfo.getNamespace());
			depDetail.put("DEP_STATUS", releaseInfo.getInfo().getStatus().getCode().toString());
			String resources = releaseInfo.getInfo().getStatus().getResources()
					.replaceAll("==> v1/", "<h2>")
					.replaceAll("==> v1beta1/", "<h2>")
					.replaceAll("==> v1/", "<h2>")
					.replaceAll("==> v1beta2/", "<h2>")
					.replaceAll("\\nNAME", "</h2>NAME")
					.replaceAll("\\n\\n", "<br><hr>")
					.replaceAll("\\n", "<br>");/*
					.replaceAll(" ", "&nbsp; ");*/
					
			//depDetail.put("DEP_RESOURCES", releaseInfo.getInfo().getStatus().getResources().replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp; "));
			depDetail.put("DEP_RESOURCES", resources);
			//depDetail.put("DEP_RESOURCES", releaseInfo.getInfo().getStatus().getResources());
			depDetail.put("DEP_NOTES", releaseInfo.getInfo().getStatus().getNotes().replaceAll("\\n", "<br>"));
			System.out.println("bb : " + releaseInfo.getInfo().getStatus().getResources().replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp; "));
		}
		mav.addObject("DEP_DETAIL", depDetail);
		mav.setViewName("deploymentDetail");
		return mav;
	}
	
	@RequestMapping(path = "/chartDelete/{depName}")
	public String helmChartDelete(@PathVariable String depName) throws Exception {

		//String ads = "https://raw.githubusercontent.com/funnylab/helm-repo/master/";

		/*URI uri = new URI(helmRepoUrl);
		ChartRepository repo = new ChartRepository(helmRepoName, uri);

		Map<String, SortedSet<Entry>> entries = repo.getIndex().getEntries();

		SortedSet<Entry> ss = entries.get(depName);
		Iterator<Entry> entIt = ss.iterator();

		URL pkgUri = null;
		while (entIt.hasNext()) {
			Entry ent = entIt.next();
			pkgUri = new URL(helmRepoUrl + ent.getFirstUri().toString());
		}*/

		/*Chart.Builder chart = null;
		try (final URLChartLoader chartLoader = new URLChartLoader()) {
			chart = chartLoader.load(pkgUri);
		}
*/
		try (final DefaultKubernetesClient client = new DefaultKubernetesClient();
				final Tiller tiller = new Tiller(client);
				final ReleaseManager releaseManager = new ReleaseManager(tiller)) {

			final UninstallReleaseRequest.Builder requestBuilder = UninstallReleaseRequest.newBuilder();
			assert requestBuilder != null;
			requestBuilder.setTimeout(300L);
			requestBuilder.setName(depName); // Set the Helm release name
			requestBuilder.setPurge(true);
			// requestBuilder.setWait(true); // Wait for Pods to be ready
			// Install the loaded chart with no user-supplied overrides.
			// To override any values, call the requestBuilder.getValuesBuilder() method,
			// and add values to the resulting Builder.

			final Future<UninstallReleaseResponse> releaseFuture = releaseManager.uninstall(requestBuilder.build());
			assert releaseFuture != null;
			System.out.println(releaseFuture.toString());
			final Release release = releaseFuture.get().getRelease();
			assert release != null;
			System.out.println(release.toString());
		}

		return "forward:/helm/deploymentList";
	}

}
