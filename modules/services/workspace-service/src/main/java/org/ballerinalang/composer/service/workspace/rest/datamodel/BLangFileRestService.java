/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.util.WorkspaceUtils;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;
import org.ballerinalang.util.program.BLangPrograms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Basic classes which exposes ballerina language object model over REST service.
 */
@Path("/ballerina")
public class BLangFileRestService {

    private static final Logger logger = LoggerFactory.getLogger(BLangFileRestService.class);

    @GET
    @Path("/model")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenLocation(@QueryParam("location") String location) throws IOException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(location));
            String response = parseJsonDataModel(stream, Paths.get(location));
            return Response.ok(response, MediaType.APPLICATION_JSON).build();
        } finally {
            if (null != stream) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateBallerinaSource(BFile bFile) throws IOException {
        InputStream stream = new ByteArrayInputStream(bFile.getContent().getBytes(StandardCharsets.UTF_8));
        return Response.status(Response.Status.OK)
                .entity(validate(stream, deriveFilePath(bFile.getFileName(), bFile.getFilePath())))
                .header("Access-Control-Allow-Origin", '*').type(MediaType.APPLICATION_JSON).build();
    }

    @OPTIONS
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateOptions() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    @POST
    @Path("/model/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenContent(BFile bFile) throws IOException {
        InputStream stream = new ByteArrayInputStream(bFile.getContent().getBytes(StandardCharsets.UTF_8));
        String response = parseJsonDataModel(stream, deriveFilePath(bFile.getFileName(), bFile.getFilePath()));
        return Response.ok(response, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", '*').build();
    }

    @OPTIONS
    @Path("/model/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    /**
     * Parses an input stream into a json model. During this parsing we are compiling the code as well.
     *
     * @param stream - The input stream.
     * @return A string which contains a json model.
     * @throws IOException
     */
    private String parseJsonDataModel(InputStream stream, java.nio.file.Path filePath) throws IOException {

        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BallerinaComposerErrorStrategy errorStrategy = new BallerinaComposerErrorStrategy();
        ballerinaParser.setErrorHandler(errorStrategy);

        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);
        BallerinaComposerModelBuilder bLangModelBuilder = new BallerinaComposerModelBuilder(packageBuilder,
                StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(true, ballerinaToken, bLangModelBuilder,
                filePath);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = bLangModelBuilder.build();

        JsonObject response = new JsonObject();
        BLangJSONModelBuilder jsonModelBuilder = new BLangJSONModelBuilder(response);
        bFile.accept(jsonModelBuilder);

        String[] dirs;
        String pkgPath = bFile.getPackagePath();

        // Filter out Default package scenario
        if (!".".equals(pkgPath)) {
            if (bFile.getPackagePath().contains(".")) {
                dirs = bFile.getPackagePath().split("\\.");
            } else {
                dirs = new String[]{bFile.getPackagePath()};
            }


            StringBuffer buf = new StringBuffer();
            java.nio.file.Path parentDir = filePath.getParent();
            for (int i = 0; i < dirs.length; ++i) {
                buf.append(dirs[i]);
                buf.append("/");
                parentDir = parentDir.getParent();
            }

            String sourcePath = buf.toString();
            Map<String, ModelPackage> packages = null;
            try {
                packages = myFunc(parentDir, Paths.get(sourcePath), pkgPath);
                Collection<ModelPackage> modelPackages = packages.values();

                Gson gson = new Gson();
                String json = gson.toJson(modelPackages);

                JsonParser parser = new JsonParser();
                JsonArray o = parser.parse(json).getAsJsonArray();
                response.add("packages", o);
            } catch (BallerinaException e) {
                // TODO : we shouldn't catch runtime exceptions. Need to validate properly before executing

                // There might be situations where program directory contains unresolvable/un-parsable .bal files. In
                // those scenarios we still needs to proceed even without package resolving. Hence ignoring the
                // exception
            }
        }
        return response.toString();
    }

    private static void listFiles(java.nio.file.Path programDirPath, List<java.nio.file.Path> packages, int depth) {
        if (depth < 0) {
            return;
        }
        DirectoryStream<java.nio.file.Path> stream = null;
        try {
            stream = Files.newDirectoryStream(programDirPath);
        } catch (IOException e) {
            return;
        }
        depth = depth - 1;
        for (java.nio.file.Path entry : stream) {
            if (Files.isDirectory(entry)) {
                listFiles(entry, packages, depth);
            }
                java.nio.file.Path file = entry.getFileName();
                if (file != null) {
                    String fileName = file.toString();
                        if (fileName.endsWith(".bal")) {
                            packages.add(entry.getParent());
                        }
                }
        }
    }

    private static Map<String, ModelPackage> myFunc(java.nio.file.Path programDirPath, java.nio.file.Path sourcePath,
                                                    String pkgPath) throws BallerinaException {
        Map<String, ModelPackage> modelPackageMap = new HashMap();
        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);

        List<java.nio.file.Path> packages = new ArrayList<>();
        int depth = 50;

        listFiles(programDirPath, packages, depth);

        for (java.nio.file.Path pkg : packages) {
            int compare = pkg.compareTo(programDirPath);
            String s = (String) pkg.toString().subSequence(pkg.toString().length() - compare + 1,
                    pkg.toString().length());
            BLangProgram bLangProgramX = new BLangProgramLoader()
                    .loadMain(programDirPath, Paths.get(s));
            String[] packageNames = {bLangProgramX.getMainPackage().getName()};
            modelPackageMap.putAll(WorkspaceUtils.getPackage(bLangProgramX, packageNames));
            //modelPackageMap.putAll(WorkspaceUtils.getAllPackages());

        }
        return modelPackageMap;


    }

    /**
     * Validates a given ballerina input
     *
     * @param stream - The input stream.
     * @return List of errors if any
     * @throws IOException
     */
    private JsonObject validate(InputStream stream, java.nio.file.Path filePath) throws IOException {

        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BallerinaComposerErrorStrategy errorStrategy = new BallerinaComposerErrorStrategy();
        ballerinaParser.setErrorHandler(errorStrategy);

        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);

        BallerinaComposerModelBuilder bLangModelBuilder = new BallerinaComposerModelBuilder(packageBuilder,
                StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder, filePath);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();

        JsonArray errors = new JsonArray();

        for (SyntaxError error : errorStrategy.getErrorTokens()) {
            errors.add(error.toJson());
        }

        JsonObject result = new JsonObject();
        result.add("errors", errors);

        return result;
    }

    private java.nio.file.Path deriveFilePath(String fileName, String filePath) {
        return Paths.get(filePath + File.separator + fileName);
    }

}
