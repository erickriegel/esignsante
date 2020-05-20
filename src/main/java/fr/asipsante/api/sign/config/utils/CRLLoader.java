/**
 * (c) Copyright 1998-2019, ASIP. All rights reserved.
 */
package fr.asipsante.api.sign.config.utils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class CRLLoader
 */
public class CRLLoader {

    /** The logger. */
    private static Logger log = LoggerFactory.getLogger(CRLLoader.class);

    /**
     * INITIAL_CAPACITY.
     */
    private static final int INITIAL_CAPACITY = 2;

    /** crl list. */
    private List<String> listCrl;

    /**
     * CRLLoader.
     *
     * @param listCrl crl list
     */
    public CRLLoader(List<String> listCrl) {
        this.listCrl = listCrl;
    }

    /**
     * CRL bundle builder.
     *
     * @param file file
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws IOException IOException
     */
    public void buildCRLBundle(File file) throws GeneralSecurityException, IOException {
        CRLLoaderImpl loader = new CRLListLoader();
        writeCrlToPEM(loader.getX509CRLs(),file);
    }

    /**
     * The Class LdapContext.
     */
    class LdapContext {
        /**
         * ldapFactoryClassName.
         */
        private final String ldapFactoryClassName;

        /**
         * LdapContext.
         */
        public LdapContext() {
            ldapFactoryClassName = "com.sun.jndi.ldap.LdapCtxFactory";
        }

        /**
         * LdapContext.
         *
         * @param ldapFactoryClassName ldapFactoryClassName
         */
        public LdapContext(String ldapFactoryClassName) {
            this.ldapFactoryClassName = ldapFactoryClassName;
        }

        /**
         * getLdapFactoryClassName.
         *
         * @return ldapFactoryClassName
         */
        public String getLdapFactoryClassName() {
            return ldapFactoryClassName;
        }
    }

    /**
     * Abstract Class CRLLoaderImpl.
     */
    abstract class CRLLoaderImpl {
        /**
         * Returns a collection of {@link X509CRL}
         *
         * @return nothing
         * @throws GeneralSecurityException GeneralSecurityException
         */
        public abstract Collection<X509CRL> getX509CRLs()
                throws GeneralSecurityException;
    }

    // Delegate to list of other CRLLoaders
    class CRLListLoader extends CRLLoaderImpl {

        /**
         * delegate list.
         */
        private final List<CRLLoaderImpl> delegates;

        /**
         * CRLListLoader.
         */
        public CRLListLoader() {
            String[] delegatePaths = new String[listCrl.size()];
            for (int i=0; i<listCrl.size(); i++) {
                delegatePaths[i] = listCrl.get(i);
            }
            this.delegates = Arrays.stream(delegatePaths)
                    .map(CRLFileLoader::new).collect(Collectors.toList());
        }

        /**
         * getX509CRLs.
         *
         * @return result
         * @throws GeneralSecurityException GeneralSecurityException
         */
        @Override
        public Collection<X509CRL> getX509CRLs()
                throws GeneralSecurityException {
            Collection<X509CRL> result = new LinkedList<>();
            for (CRLLoaderImpl delegate : delegates) {
                result.addAll(delegate.getX509CRLs());
            }
            return result;
        }
    }

    /**
     * The Class CRLFileLoader
     */
    class CRLFileLoader extends CRLLoaderImpl {

        /* cRLPath. */
        private final String cRLPath;
        /* ldapContext. */
        private final LdapContext ldapContext;

        /**
         * CRLFileLoader.
         *
         * @param cRLPath cRLPath
         */
        public CRLFileLoader(String cRLPath) {
            this.cRLPath = cRLPath;
            ldapContext = new LdapContext();
        }

        /**
         * CRLFileLoader.
         *
         * @param cRLPath cRLPath
         * @param ldapContext ldapContext
         */
        public CRLFileLoader(String cRLPath, LdapContext ldapContext) {
            this.cRLPath = cRLPath;
            this.ldapContext = ldapContext;

            if (ldapContext == null) {
                throw new NullPointerException("Context cannot be null");
            }
        }

        /**
         * getX509CRLs.
         *
         * @return crlColl
         * @throws GeneralSecurityException GeneralSecurityException
         */
        public Collection<X509CRL> getX509CRLs() throws GeneralSecurityException {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<X509CRL> crlColl = null;
            if (cRLPath != null) {
                if (cRLPath.startsWith("http") || cRLPath.startsWith("https")) {
                    // load CRL using remote URI
                    try {
                        crlColl = loadFromURI(cf, new URI(cRLPath));
                    } catch (URISyntaxException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                    }
                } else if (cRLPath.startsWith("ldap")) {
                    // load CRL from LDAP
                    try {
                        crlColl = loadCRLFromLDAP(cf, new URI(cRLPath));
                    } catch (URISyntaxException e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                    }
                }
            }
            if (crlColl == null || crlColl.size() == 0) {
                String message = String.format("Unable to load CRL from \"%s\"",
                        cRLPath);
                throw new GeneralSecurityException(message);
            }
            return crlColl;
        }

        /**
         * loadFromURI.
         *
         * @param cf cf
         * @param remoteURI remoteURI
         * @return Collection
         * @throws GeneralSecurityException GeneralSecurityException
         */
        private Collection<X509CRL> loadFromURI(CertificateFactory cf, URI remoteURI) throws GeneralSecurityException {
            Collection<X509CRL> collection = Collections.emptyList();
            try {
                log.debug("Loading CRL from %s", remoteURI.toString());

                URLConnection conn = remoteURI.toURL().openConnection();
                conn.setDoInput(true);
                conn.setUseCaches(false);
                X509CRL crl = loadFromStream(cf, conn.getInputStream());
                collection = Collections.singleton(crl);
            } catch (IOException ex) {
                log.error(ExceptionUtils.getFullStackTrace(ex));
            }
            return collection;

        }

        /**
         * loadCRLFromLDAP.
         *
         * @param cf cf
         * @param remoteURI remoteURI
         * @return Collection
         * @throws GeneralSecurityException GeneralSecurityException
         */
        private Collection<X509CRL> loadCRLFromLDAP(CertificateFactory cf, URI remoteURI)
                throws GeneralSecurityException {
            Hashtable<String, String> env = new Hashtable<>(INITIAL_CAPACITY);
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    ldapContext.getLdapFactoryClassName());
            env.put(Context.PROVIDER_URL, remoteURI.toString());

            try {
                DirContext ctx = new InitialDirContext(env);
                try {
                    Attributes attrs = ctx.getAttributes("");
                    Attribute cRLAttribute = attrs
                            .get("certificateRevocationList;binary");
                    byte[] data = (byte[]) cRLAttribute.get();
                    if (data == null || data.length == 0) {
                        throw new CertificateException(String.format(
                                "Failed to download CRL from \"%s\"",
                                remoteURI.toString()));
                    }
                    X509CRL crl = loadFromStream(cf,
                            new ByteArrayInputStream(data));
                    return Collections.singleton(crl);
                } finally {
                    ctx.close();
                }
            } catch (NamingException | IOException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
            }

            return Collections.emptyList();
        }

        /**
         * loadFromStream.
         *
         * @param cf cf
         * @param is is
         * @return Collection
         * @throws IOException IOException
         * @throws CRLException CRLException
         */
        private X509CRL loadFromStream(CertificateFactory cf, InputStream is) throws IOException, CRLException {
            DataInputStream dis = new DataInputStream(is);
            X509CRL crl = (X509CRL) cf.generateCRL(dis);
            dis.close();
            return crl;
        }
    }

    /**
     * writeCrlToPEM.
     *
     * @param crls crls
     * @param file file
     */
    private void writeCrlToPEM(final Collection<X509CRL> crls, File file) {
        try (PemWriter pem = new PemWriter(new FileWriter(file))) {
            for (X509CRL x509crl : crls) {
                PemObjectGenerator pemObjectGenerator = new PemObject("X509 CRL", x509crl.getEncoded());
                pem.writeObject(pemObjectGenerator);
            }
            pem.flush();
        } catch (IOException | CRLException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

}
