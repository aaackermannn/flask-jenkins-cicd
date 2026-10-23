// Задания Pipeline script from SCM для базового и усложнённого вариантов
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.GitSCM
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

def repoUrl = System.getenv('REPO_URL') ?: 'file:///repos/flask-jenkins-cicd'
def jobs = [
    ['basic-pipeline', 'basic/Jenkinsfile', 'Базовый вариант: конвейер из методических указаний'],
    ['flask-cicd', 'Jenkinsfile', 'Усложнённый вариант: Docker-образ Flask-приложения, тесты, staging и production'],
]

def jenkins = Jenkins.get()
jobs.each { name, scriptPath, description ->
    def job = jenkins.getItem(name) ?: jenkins.createProject(WorkflowJob, name)
    // при каждом старте источник задания приводится к REPO_URL (например, после переезда на GitHub)
    def scm = new GitSCM(GitSCM.createRepoList(repoUrl, null), [new BranchSpec('*/main')], null, null, [])
    job.setDefinition(new CpsScmFlowDefinition(scm, scriptPath))
    job.setDescription(description)
    job.save()
}
