// Учётная запись администратора и права доступа (вместо мастера первоначальной настройки)
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.install.InstallState
import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration

def jenkins = Jenkins.get()
def password = System.getenv('JENKINS_ADMIN_PASSWORD')

def realm = new HudsonPrivateSecurityRealm(false)
if (realm.getUser('admin') == null) {
    realm.createAccount('admin', password)
}
jenkins.setSecurityRealm(realm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
jenkins.setAuthorizationStrategy(strategy)

jenkins.setInstallState(InstallState.INITIAL_SETUP_COMPLETED)
JenkinsLocationConfiguration.get().setUrl('http://localhost:8080/')
jenkins.save()
