package lucene.index;

import io.FileOperation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Lu Xugang
 * @date 2019-03-18 20:52
 */
public class IndexFileWithLessFieldValues {
  private Directory directory;

  {
    try {
      FileOperation.deleteFile("./data");
      directory = new MMapDirectory(Paths.get("./data"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private IndexWriter indexWriter;

  public void doIndex() throws Exception {
    FieldType type = new FieldType();
    type.setStored(true);
    type.setStoreTermVectors(true);
    type.setStoreTermVectorPositions(true);
    type.setStoreTermVectorPayloads(true);
    type.setStoreTermVectorOffsets(true);
    type.setTokenized(true);
    type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

    Analyzer analyzer = new WhitespaceAnalyzer();
    IndexWriterConfig conf = new IndexWriterConfig(analyzer);
    SortField indexSortField = new SortField("sortByNumber", SortField.Type.LONG);
    Sort indexSort = new Sort(indexSortField);;
//    conf.setIndexSort(indexSort);


    conf.setUseCompoundFile(false);

    indexWriter = new IndexWriter(directory, conf);
    int count = 0;
    while (count++ < 1) {

      // 文档0
      Document doc = new Document();
      doc.add(new Field("author", "aab b aab aabbcc ", type));
      doc.add(new Field("content", "a b", type));
      doc.add(new IntPoint("intPoitn", 3, 4, 6));
      indexWriter.addDocument(doc);

      // 文档1
      doc = new Document();
      doc.add(new TextField("author", "a", Field.Store.YES));
      doc.add(new TextField("content", "a b c h", Field.Store.YES));
      doc.add(new TextField("title", "d a", Field.Store.YES));
      doc.add(new NumericDocValuesField("sortByNumber", -1));
      doc.add(new IntPoint("intPoitn", 3, 5, 6));
      indexWriter.addDocument(doc);

      // 文档2
      doc = new Document();
      doc.add(new TextField("author", "aab aab aabb ", Field.Store.YES));
      doc.add(new TextField("content", "a c b e", Field.Store.YES));
      doc.add(new NumericDocValuesField("sortByNumber", 4));
      indexWriter.addDocument(doc);

      // 文档3
      doc = new Document();
      doc.add(new TextField("author", "aabb ", Field.Store.YES));
      doc.add(new TextField("content", "b c e", Field.Store.YES));
      doc.add(new NumericDocValuesField("sortByNumber", 1));
      indexWriter.addDocument(doc);

      // 文档4
      doc = new Document();
      doc.add(new TextField("author", "aab", Field.Store.YES));
      doc.add(new TextField("content", "a c e f g d", Field.Store.YES));
      indexWriter.addDocument(doc);

      indexWriter.deleteDocuments(new Term("author", "aab"));
      indexWriter.updateNumericDocValue(new Term("author", "aabb"), "sortByNumber", 3);

      indexWriter.flush();
    }
    indexWriter.commit();
//          indexWriter.updateNumericDocValue(new Term("author", "a"), "sortByNumber", 99);
    indexWriter.commit();


    // Per-top-reader state:

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    builder.add(new TermQuery(new Term("content", "a")), BooleanClause.Occur.SHOULD);
    builder.add(new TermQuery(new Term("content", "b")), BooleanClause.Occur.SHOULD);
    builder.add(new TermQuery(new Term("content", "c")), BooleanClause.Occur.SHOULD);
    builder.add(new TermQuery(new Term("content", "h")), BooleanClause.Occur.SHOULD);
    builder.add(new TermQuery(new Term("content", "e")), BooleanClause.Occur.SHOULD);
    builder.setMinimumNumberShouldMatch(2);

    DirectoryReader reader = DirectoryReader.open(indexWriter);
    reader.maxDoc();
    IndexSearcher indexSearcher = new IndexSearcher(reader);
    SortField searchSortField = new SortField("sortByNumber", SortField.Type.LONG);
    Sort searchSort = new Sort(searchSortField);

    TopFieldCollector collector = TopFieldCollector.create(searchSort, 2, true, false, false, false);
    indexSearcher.search(builder.build(),  collector);

//    TopFieldDocs fieldDocs = indexSearcher.search(new MatchAllDocsQuery(), 5, searchSort);

    System.out.printf("ha");

  }

  public static void main(String[] args) throws Exception{
    IndexFileWithLessFieldValues test = new IndexFileWithLessFieldValues();
    test.doIndex();
//    FileChannel         channel = FileChannel.open(Paths.get("/Users/luxugang/project/Lucene-7.5.0/LuceneDemo/data/write.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
//    FileLock fileLock = channel.tryLock();
//    System.out.printf("abc");
//    System.out.printf("abc");
//    System.out.printf("abc");
//    fileLock.release();
//    FileLock fileLock1 = FileChannel.open(Paths.get("/Users/luxugang/project/Lucene-7.5.0/LuceneDemo/data/write.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE).lock();
//    System.out.printf("abc");
  }

}
