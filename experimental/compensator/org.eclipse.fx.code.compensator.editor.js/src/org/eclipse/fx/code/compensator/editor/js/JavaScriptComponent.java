package org.eclipse.fx.code.compensator.editor.js;

import org.eclipse.fx.code.compensator.editor.ContentTypeProvider;
import org.eclipse.fx.code.compensator.editor.Input;
import org.eclipse.fx.code.compensator.editor.js.scanner.FastJavaScriptPartitionScanner;
import org.eclipse.fx.code.compensator.editor.js.scanner.IJavaScriptPartitions;
import org.eclipse.fx.code.compensator.editor.services.PartitionerFactory;
import org.eclipse.fx.code.compensator.editor.services.SourceViewerConfigurationFactory;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class JavaScriptComponent implements PartitionerFactory, SourceViewerConfigurationFactory {
	private final static String[] LEGAL_CONTENT_TYPES= new String[] {
		IJavaScriptPartitions.JAVA_DOC,
		IJavaScriptPartitions.JAVA_MULTI_LINE_COMMENT,
		IJavaScriptPartitions.JAVA_SINGLE_LINE_COMMENT,
		IJavaScriptPartitions.JAVA_STRING,
		IJavaScriptPartitions.JAVA_CHARACTER
	};
	@Override
	public SourceViewerConfiguration createConfiguration(Input<?> input) {
		return new JavaScriptSourceConfiguration();
	}

	@Override
	public boolean applies(Input<?> input) {
		if( input instanceof ContentTypeProvider ) {
			String contentType = ((ContentTypeProvider) input).getContentType();
			return ContentTypeProvider.JAVASCRIPT.equals(contentType);
		}
		return false;
	}

	@Override
	public IDocumentPartitioner createPartitioner(Input<?> input) {
		return new FastPartitioner(
				new FastJavaScriptPartitionScanner(),
				LEGAL_CONTENT_TYPES);
	}
}