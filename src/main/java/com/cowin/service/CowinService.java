package com.cowin.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cowin.config.CowinConfig;
import com.cowin.model.Centres;
import com.cowin.model.Data;
import com.cowin.model.Session;
//import com.cowin.telegram.VacNearMeBot;
import com.cowin.util.CowinUtil;

@Service
public class CowinService {
	Logger log = LoggerFactory.getLogger(CowinService.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private CowinConfig config;
	
	@Value("${chat.id}")
	private String chatId;
	
	@Value("${token.id}")
	private String tokenId;
	
	HashMap<String, Integer> map = new HashMap<>();
	
	@Scheduled(fixedRate = 3000)//this scheduler will be called every 3 sec
	public List<String> getData() {
		CowinUtil util = new CowinUtil();
		//VacNearMeBot bot = new VacNearMeBot();
		HttpHeaders headers = config.getHeaders();
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		String date = df.format(new Date());
		String districtId = "286";//District Id of Udupi
		ResponseEntity<Data> response = restTemplate.exchange("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id="+districtId+"&date="+date, HttpMethod.GET, entity, Data.class);
		StringBuffer res = new StringBuffer("");
		List<String> result = new ArrayList<>();
		Centres[] centers = response.getBody().getCenters();
		for(Centres cen : centers) {
			Session[] session = cen.getSessions();
			for(Session sess : session) {
				if(sess.getAvailable_capacity() > 0 && sess.getMin_age_limit().equals(18) && sess.getAvailable_capacity_dose1() > 0) {
					res.append(cen.getName()+" - ").append("Pin: "+cen.getPincode()+" ")
					.append("Vaccine : "+sess.getVaccine()+" ").append("No. of slots : "+sess.getAvailable_capacity()+" ")
					.append("Available Capacity for Dose 1 : "+sess.getAvailable_capacity_dose1()+" ")
					.append("Available Capacity for Dose 2 : "+sess.getAvailable_capacity_dose2()+" ")
					.append("Date : "+sess.getDate());
					
					if(map.containsKey(sess.getSession_id())) {
						if(sess.getAvailable_capacity() == 0)
							map.remove(sess.getSession_id());
					}else {
						map.put(sess.getSession_id(), sess.getAvailable_capacity());
						log.info(res.toString());
						//bot.sendMsg(chatId, res.toString());
						restTemplate.exchange("https://api.telegram.org/bot"+tokenId+/*place your bot's token id in application.properties*/"/sendMessage?chat_id="+chatId+/*place your group's chat id in application.properties*/"&text="+res.toString(), HttpMethod.GET, entity, String.class);
						/*try {
							util.sendmail(res.toString());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
					}
				}
				if(!res.toString().equals("")) {
					result.add(res.toString());
				}
				res = new StringBuffer("");
			}
		}
		return result;
	}

}
