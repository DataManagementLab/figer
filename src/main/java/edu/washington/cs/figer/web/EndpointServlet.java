package edu.washington.cs.figer.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.washington.cs.figer.FigerSystem;
import edu.washington.cs.figer.analysis.Preprocessing;

/**
 * 
 * @author Xiao Ling
 */

//@WebServlet(urlPatterns = Array("/"))
public class EndpointServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2498702085857068707L;

	private final static Logger logger = LoggerFactory
			.getLogger(EndpointServlet.class);

	static FigerSystem figer = null;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String text = request.getParameter("text");

		if (text == null) {
			try {
				response.getWriter().write("{'status':400, 'error':'No text passed'}");
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (figer == null) {
			figer = FigerSystem.instance();
		}

		try {
			Annotation annotation = new Annotation(text);
			if (Preprocessing.pipeline == null) {
				Preprocessing.initPipeline(false, false);
			}
			Preprocessing.pipeline.annotate(annotation);

			int sentId = 0;
			response.getWriter().write(
					"{\"status\":200, \"data\": [");

			List<Integer> sentenceStarts = new ArrayList<>();

			boolean first_nugget = true;

			// Loop over all sentences
			for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
				Integer sentence_offset = sentence.get(CharacterOffsetBeginAnnotation.class);
				sentenceStarts.add(sentence_offset);

				List<String> words = new ArrayList<>();
				List<Integer> start_positions = new ArrayList<>();
				List<Integer> end_positions = new ArrayList<>();
				List<String> tags = new ArrayList<>();

				List<StringBuilder> nugget_words = new ArrayList<>();
				List<String> nugget_labels = new ArrayList<>();
				List<String> nugget_tags = new ArrayList<>();
				List<Integer> nuggets_start_positions = new ArrayList<>();
				List<Integer> nuggets_end_positions = new ArrayList<>();

				// Identify all tokens in the current sentence...
				for (CoreLabel label : sentence.get(TokensAnnotation.class)) {
					// ... and store relevant information like start and end points, POS tags and the surface form
					words.add(label.originalText());
					start_positions.add(label.beginPosition());
					end_positions.add(label.endPosition());
					tags.add(label.tag());
				}

				// Identify named entities and get their word offsets (start and end index of each mention as pair)
				List<Pair<Integer, Integer>> entityMentionOffsets = FigerSystem
						.getNamedEntityMentions(sentence);

				// For all entities...
				for (Pair<Integer, Integer> offset : entityMentionOffsets) {

					// ... store start point...
					nuggets_start_positions.add(start_positions.get(offset.first));
					nugget_tags.add(tags.get(offset.first));

					// ... reconstruct surface form...
					StringBuilder mention = new StringBuilder();
					for (int i = offset.first; i < offset.second; i++) {
						mention.append(words.get(i)).append(" ");
					}
					nugget_words.add(mention);

					// ...store endpoint...
					nuggets_end_positions.add(end_positions.get(offset.second-1));

					// ... and determine fine-grained label(s)
					String label = figer.predict(annotation, sentId,
							offset.first, offset.second);
					nugget_labels.add(label);
				}

				// Output all nuggets in JSON format
				for (int i = 0; i < nugget_words.size(); i++) {
					if(!first_nugget)
						response.getWriter().write(", ");
					else
						first_nugget = false;
					response.getWriter().write("{\"mention\": \"" +
							nugget_words.get(i).toString() +
							"\", \"label\": \"" +
							nugget_labels.get(i) +
							"\", \"tag\": \"" +
							nugget_tags.get(i) +
							"\", \"start_char\": " +
							nuggets_start_positions.get(i) +
							", \"end_char\": " +
							nuggets_end_positions.get(i) +
							"}");
				}
				sentId++;
			}

			// Output sentence offsets
			response.getWriter().write("], \"sentence_offsets\": [");
			for (int i = 0; i < sentenceStarts.size(); i++) {
				if(i > 0)
					response.getWriter().write(", ");
				response.getWriter().write(sentenceStarts.get(i).toString());
			}
			response.getWriter().write("]}");
		} catch (Exception e) {
			e.printStackTrace();
			// response.getWriter().write("ERROR: ");
		}
	}
}
