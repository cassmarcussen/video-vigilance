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
           /* BlobKey blobKey = new BlobKey(request.getParameter("blobkey"));
            blobstoreService.serve(blobKey, response);*/
           // BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
            BlobKey blobKey = blobstoreService.createGsBlobKey(
                "/gs/keyframe-images-for-effect/" + "keyframe-image-" + fileNum);
            fileNum++;
            blobstoreService.serve(blobKey, response);
        }
}