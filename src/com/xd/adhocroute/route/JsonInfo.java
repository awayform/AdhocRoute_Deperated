package com.xd.adhocroute.route;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.xd.adhocroute.data.Gateway;
import com.xd.adhocroute.data.HNA;
import com.xd.adhocroute.data.Interface;
import com.xd.adhocroute.data.Link;
import com.xd.adhocroute.data.MID;
import com.xd.adhocroute.data.Neighbor;
import com.xd.adhocroute.data.Node;
import com.xd.adhocroute.data.OlsrDataDump;
import com.xd.adhocroute.data.Plugin;
import com.xd.adhocroute.data.Route;
import com.xd.adhocroute.log.Lg;

public class JsonInfo {

	private String lastCommand = "";

	String host = "127.0.0.1";
	
	// 根据配置的IP进行修改
	int port = 8118;

	ObjectMapper mapper = null;

	final Set<String> supportedCommands = new HashSet<String>(
			Arrays.asList(new String[] {
					"all",
					"runtime",
					"startup",
					"gateways",
					"hna",
					"interfaces",
					"links",
					"mid",
					"neighbors",
					"routes",
					"topology",
					"runtime",
					"config",
					"plugins",
					"olsrd.conf",
			}));

	public JsonInfo() {
	}

	public JsonInfo(String sethost) {
		host = sethost;
	}

	public JsonInfo(String sethost, int setport) {
		host = sethost;
		port = setport;
	}

	private boolean isCommandStringValid(String cmdString) {
		boolean isValid = true;
		if (!cmdString.equals(lastCommand)) {
			lastCommand = cmdString;
			for (String s : cmdString.split("/")) {
				if ( !s.equals("") && !supportedCommands.contains(s)) {
					Lg.d("Unsupported command: " + s);
					isValid = false;
				}
			}
		}
		return isValid;
	}
	
	String[] request(String req) throws IOException {
		Socket sock = null;
		BufferedReader in = null;
		PrintWriter out = null;
		List<String> retlist = new ArrayList<String>();

		try {
			sock = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()), 8192);
			out = new PrintWriter(sock.getOutputStream(), true);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + host);
			return new String[0];
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for socket to " + host + ":"
					+ Integer.toString(port));
			return new String[0];
		}
		out.println(req);
		String line;
		while ((line = in.readLine()) != null) {
			if (!line.equals(""))
				retlist.add(line);
		}
		out.close();
		in.close();
		sock.close();

		return retlist.toArray(new String[retlist.size()]);
	}
	public String command(String cmdString) {
		String[] data = new String[0];
		String ret = "";

		isCommandStringValid(cmdString);
		try {
			data = request(cmdString);
		} catch (IOException e) {
			System.err.println("Failed to read data from " + host + ":"
					+ Integer.toString(port));
		}
		for (String s : data) {
			ret += s + "\n";
		}
		return ret;
	}
	
	// 最好修改一下，ret为null表明未请求到数据
	public OlsrDataDump parseCommand(String cmd) {
		if (mapper == null)
			mapper = new ObjectMapper();
		OlsrDataDump ret = new OlsrDataDump();
		try {
			String dump = command(cmd);
			if (! dump.contentEquals(""))
				// 等于""说明请求数据失败了
				ret = mapper.readValue(dump, OlsrDataDump.class);
			ret.setRaw(dump);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (ret.config == null)
			ret.config = new com.xd.adhocroute.data.Config();
		if (ret.gateways == null)
			ret.gateways = Collections.emptyList();
		if (ret.hna == null)
			ret.hna = Collections.emptyList();
		if (ret.interfaces == null)
			ret.interfaces = Collections.emptyList();
		if (ret.links == null)
			ret.links = Collections.emptyList();
		if (ret.mid == null)
			ret.mid = Collections.emptyList();
		if (ret.neighbors == null)
			ret.neighbors = Collections.emptyList();
		if (ret.topology == null)
			ret.topology = Collections.emptyList();
		if (ret.plugins == null)
			ret.plugins = Collections.emptyList();
		if (ret.routes == null)
			ret.routes = Collections.emptyList();
		return ret;
	}

	public OlsrDataDump all() {
		return parseCommand("/all");
	}
	public OlsrDataDump runtime() {
		return parseCommand("/interfaces");
	}
	public OlsrDataDump startup() {
		return parseCommand("/interfaces");
	}
	public Collection<Neighbor> neighbors() {
		return parseCommand("/neighbors").neighbors;
	}
	public Collection<Link> links() {
		return parseCommand("/links").links;
	}
	public Collection<Route> routes() {
		return parseCommand("/routes").routes;
	}
	public Collection<HNA> hna() {
		return parseCommand("/hna").hna;
	}
	public Collection<MID> mid() {
		return parseCommand("/mid").mid;
	}
	public Collection<Node> topology() {
		return parseCommand("/topology").topology;
	}
	public Collection<Interface> interfaces() {
		return parseCommand("/interfaces").interfaces;
	}
	public Collection<Gateway> gateways() {
		return parseCommand("/gateways").gateways;
	}

	public com.xd.adhocroute.data.Config config() {
		return parseCommand("/config").config;
	}

	public Collection<Plugin> plugins() {
		return parseCommand("/plugins").plugins;
	}

	public String olsrdconf() {
		return command("/olsrd.conf");
	}
	
	public static void main(String[] args) throws IOException {
		JsonInfo jsoninfo = new JsonInfo();
		OlsrDataDump dump = jsoninfo.all();
		Lg.d("gateways:");
		for (Gateway g : dump.gateways)
			Lg.d("\t" + g.ipAddress);
		Lg.d("hna:");
		for (HNA h : dump.hna)
			Lg.d("\t" + h.destination);
		Lg.d("Interfaces:");
		for (Interface i : dump.interfaces)
			Lg.d("\t" + i.name);
		Lg.d("Links:");
		for (Link l : dump.links)
			Lg.d("\t" + l.localIP + " <--> " + l.remoteIP);
		Lg.d("MID:");
		for (MID m : dump.mid)
			Lg.d("\t" + m.ipAddress);
		Lg.d("Neighbors:");
		for (Neighbor n : dump.neighbors)
			Lg.d("\t" + n.ipv4Address);
		Lg.d("Plugins:");
		for (Plugin p : dump.plugins)
			Lg.d("\t" + p.plugin);
		Lg.d("Routes:");
		for (Route r : dump.routes)
			Lg.d("\t" + r.destination);
		Lg.d("Topology:");
		for (Node node : dump.topology)
			Lg.d("\t" + node.destinationIP);
	}
}
