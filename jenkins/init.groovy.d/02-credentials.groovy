// Секрет demo-access-key (Secret text), который использует basic/Jenkinsfile
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import hudson.util.Secret
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

def provider = SystemCredentialsProvider.getInstance()
if (!provider.getCredentials().any { it.id == 'demo-access-key' }) {
    def secret = new StringCredentialsImpl(
        CredentialsScope.GLOBAL,
        'demo-access-key',
        'Демонстрационный ключ доступа (ПР4)',
        Secret.fromString('demo-' + UUID.randomUUID().toString().take(8)))
    provider.getStore().addCredentials(Domain.global(), secret)
}
