import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/serve")
public class ServeKeyFrameImage extends HttpServlet {
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private int fileNum = 1;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
            BlobKey blobKey = blobstoreService.createGsBlobKey(
                "/gs/keyframe-images-for-effect/" + fileNum + "-keyframe-image");
            fileNum++;
            blobstoreService.serve(blobKey, response);

            
        }
}